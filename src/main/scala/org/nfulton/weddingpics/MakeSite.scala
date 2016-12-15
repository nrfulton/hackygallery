package org.nfulton.weddingpics

import java.io.{File, PrintWriter}
import java.nio.file.Path

import com.sksamuel.scrimage.{Image, ImageParseException}
import com.sksamuel.scrimage.nio.JpegWriter

object MakeSite {
  val IMAGES_PER_PAGE = 20
  val THUMBS_DIR = "thumbs"

  def main(args : Array[String]) : Unit = {
    assert(args.length >= 1,
      s"Usage: scala MakeSite.scala <location of images directory> [pass a second arg if you want to skip thumbnail creation.]")
    assert(new File(THUMBS_DIR).isDirectory,
      s"Must have a directory called ${THUMBS_DIR}.")
    assert(new File("template.html").exists,
      "Must have a template file called template.html") //see README.md for an explanation of what this file should contain.
    assert(new File("template.html").canRead,
      "teamplate.html must be readable.")

    val dir        = new File(args(0))
    val imageFiles = dir.listFiles.filter(f => f.isFile && f.canRead && f.getName.endsWith(".jpg"))

    println(s"Found ${imageFiles.length} files")

    //Get each of the .jpg files in the user-specified directory and generate
    //thumbnails in parallel.
    imageFiles.par.map(x => {
      try {
        Some((x.getName, makeThumbnail(x)))
      } catch {
        case e:ImageParseException => {
          println(s"Failed to parse ${x.getAbsolutePath} as image.")
          None
        }
        case e:Throwable => {
          println(s"Uknown error: ${e.toString}")
          None
        }
      }
    }).filter(_.isDefined)
      .map(_.get)
      .toList
      .sortBy(x => intOrNegOne(x._1.split("\\.")(0)))
      .grouped(IMAGES_PER_PAGE)
      .toList
      .zipWithIndex
      .map(makePage(_))
  }

  /** Makes a thumbnail of img and places it in wedding_images/name_thumb.jpg
    * If file already exists, skips creation.
    * @return string with relative location of thumbnail file. */
  private def makeThumbnail(img: File) : String = {
    if(!new File(outputFileName(img)).exists) {
      println(s"Creating a thumbnail for ${img.getName}");
      implicit val writer = JpegWriter().withCompression(50)
      // Images are 5472x3648?
      val thumbnail = Image.fromFile(img).fit(342,248)
      thumbnail.output(outputFileName(img)).toFile
    }

    outputFileName(img)
  }

  /** There are some images named 0-12.jpg so that their names don't parse as
   * ints. But these are all really early pics so they should go at the
   * front... */
  private def intOrNegOne(s: String): Int = try {
    Integer.parseInt(s)
  } catch {
    case e:  Throwable => -1
  }

  /** name.JPG ~> wedding_images/name_thumb.jpg
    * Throws a [[MatchError]] if input is not a file whose name has the form name.JPG where
    * name contains no dots. */
  private def outputFileName(img: File) = {
    val Array(prefix, suffix) = img.getName.split("\\.")
    THUMBS_DIR + File.separator + prefix + "_thumb.jpg"
  }

  /** Makes the HTML pages. */
  private def makePage(imgSetAndIndex : (List[(String, String)], Int)) = {
    val (imgSet, idx) = imgSetAndIndex
    println(s"Creating ${idx}.html");

    val pageContents = scala.io.Source.fromFile("template.html").getLines().foldLeft("")((page, line) => {
      page + (line match {
        case "IMAGES" =>
          imgSet.foldLeft("")((images, nextImages) =>
            images + "<a href='full/" + nextImages._1 + "' target='_blank'><img src=\"" + nextImages._2 + "\"></a><span id='divider'>&nbsp;</span>"
          )
        case "NAVIGATION" =>
          "<center><h1>Gallery</h1></center>" +
          "<font size=4>Clicking on an image will open a full-sized copy of the image in a new tab.<br/>Click on the arrows to move between pages.</font>" +
          "<center><font size=4>" +
          (if(idx == 0) "&nbsp;&nbsp;" else s"<a href='${idx-1}.html'>&lt;&lt;</a>")  +
          s"&nbsp;&nbsp;${idx+1} / 26&nbsp;&nbsp;" +
          s"<a href='${idx+1}.html'>&gt;&gt;</a>" +
          "</font></center>"
        case _ => line
      })
    })

    val outputFile = new File(s"$idx.html")
    val w = new PrintWriter(outputFile);
    w.write(pageContents);
    w.close

    outputFile
  }
}

