/*
# For loops with images

You can also process images in loops. For example,
we blur an image with different sigma values. 
Note how the `sigma` variable becomes part of the 
`run()` command by adding it.

*/

run("Blobs (25K)");

for (sigma = 2; sigma < 10; sigma += 2) {
	run("Duplicate...", " ");
	run("Gaussian Blur...", "sigma=" + sigma);
}
