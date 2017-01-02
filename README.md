# Pac4j-async

## What is pac4j-async?

Pac4j-async is an asynchronous candidate implementation of the pac4j framework for use 
with asynchronous and non-blocking web frameworks and toolkits.

The existing pac4j framework is blocking at its heart, limiting scalability when used
with a non-blocking framework such as vert.x.

This implementation is an attempt to try and tease out some of the key differences 
required to produce an asychronous version of the API which can be used with a non-blocking
API with minimal use of executor pools etc to run blocking code.

It will not prevent use of blocking code, but the aim is to cleanly enable use of non-blocking APIs
without having to wrap them in blocking code. However, the primary initial aim is 
to act as an experiment to try and identify changes we can make to the core pac4j
APIs to maximise code reuse between the sync and async versions.

