/*
# For loops

In ImageJ macro you can execute some operations repeatedly by using a for-loop.
This is how they work:

```
for (<Initializer>, <Condition>, <Iterator>)
```

1. the `Initializer` is executed once.
2. the `Condition` is checked. If it is true, the code between {} is executed.
3. the `Iterator` is executed
4. Jump back to Step 2.

In the following example this repeats until the variable i is no longer smaller 
than 10.
*/
for (i = 0; i < 10; i += 2) {
	print(i);
}
