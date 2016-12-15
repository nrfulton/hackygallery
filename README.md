Very hacky HTML image gallery generator for generating image galleries.
Constraints on output: 
 * use as little bandwidth as possible for everything but full gallery 
 * Work on extremely resource constrained (aka old/crappy/don't care if stolen) machine
 * completely static and no javascript (see above, but also cheap hosting)

Constraints on script:
 * Process images in parallel (lots and lots of images, need to regenerate frequently)
 * Multi-platform

Assumes all images have ~ alphanumeric names and end in .jpg. 

To use:
 * Place your full-sized .jpg images in full/
 * Run assembled JAR in this directory.
 * Copy all HTML files, full/ directory, and thumbs/ directory into web root.
 * Add aliases for n+1.html and index.html pointing to 0.html.
