# CAPSOS (Cellular Automata, Particle Swarm Optimization, Synth)

CAPSOS aims primarily to be a personal exploration in the interactions
between complex systems phenomena such as Cellular Automata and
swarming group behavior by way of Particle Swarm Optimization. 

CAPSOS is artistic in nature, and makes use of the fantastic Quil and
Overtone libraries for Clojure.

This is very much a work in progress. There is a bit of namespace
clutter going on for the time being, as ideas are worked out.

## Usage

For now, there is no command line, but that will be added at some
point. 

Check out the `capsos.core` namespace. There are a few `(comments)`
sections that contain the s-expressions necessary to start the system
and start playing around. Quite a few of the namespaces are littered
with `(comments)` as things are worked out and I figure out where I
want this to go.

## CA

The currently implemented rules are for "Game of Life". The cell grid
can be configured to be either flat, or toroidal. On the flat board,
cells can not extend past the edge, which is a bit non-standard with
regards to *GoL* implementations. I made this choice mostly for visual
/ artistic reasons. 

## PSO

## PSO and CA Interactions

