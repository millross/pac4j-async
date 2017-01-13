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

Technical notes for reference:-

A small project was used to investigate some of the characteristics of CompletableFutures
(e.g. can they be completed before attaching subsequent processing and have that processing
trigger immediately). This project can be found [here](https://github.com/millross/vertx-completablefuture-demo) 
and led to the following conclusions

* If followup procesisng is attached to a CompletableFuture which has already been completed then 
the processing will be triggered immediately. Therefore it is legitimate for asynchronous functions
to create and return CompletableFutures as there is no requirement for
preconfigured processing.
* Where a framework such as vert.x is using an async wrapper to wrap blocking code then the callback
supplied to that wrapper should complete the associated CompletableFuture, not the contained
blocking code. This is important to ensure that subsequent processing occurs on the correct thread
and context.
* Where a CompletableFuture completes, subsequent processing occurs on the thread which called the .complete
or .completeExceptionally method. This means that completing immediately is fine (so non-blocking code can 
complete the associated future immediatley)
* Where subsequent processing is attached to a CompletableFuture which has already completed, the 
processing is perfomed on the thread which attached the subsequent processing.