/*
# Hello world
This is just some text
* an a bullet point 

In _italic_ and __bold__

Let's start with some math
*/

a = 5;

print(a);

/*
 
Now let's open an image
*/
run("Blobs (25K)");
/*
 
This is how we can blur it
*/
run("Duplicate...", " ");

run("Gaussian Blur...", "sigma=4");

/*
 
Converting an image in a binary image using thresholding works like this:
*/
run("Duplicate...", " ");

setAutoThreshold("Default");
setOption("BlackBackground", true);
run("Convert to Mask");

/*

In order to label individual objects, we use the Particle analyser
*/

run("Analyze Particles...", "  show=[Count Masks]");
run("glasbey", "display=[Count Masks of blobs.gif] view=net.imagej.display.DefaultDatasetView@319f6c19");

/*
Let's open a stack
*/

run("T1 Head (2.4M, 16-bits)");

/*
And navigate through it

*/
stack = getTitle();

Stack.setSlice(20);
run("Duplicate...", " ");

selectWindow(stack);
Stack.setSlice(40);
run("Duplicate...", " ");

selectWindow(stack);
Stack.setSlice(60);
run("Duplicate...", " ");

/*
Bye.
*/