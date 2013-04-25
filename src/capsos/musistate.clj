(ns capsos.musistate
  (:require [capsos.state  :as state]
            [capsos.quil   :as gfx]
            [capsos.pso    :as pso]
            [capsos.audio  :as audio])
  (:use [leipzig.scale]))

;; State / Music / Utility Namespace


;; Tonality is a bad name for this, data structure, it's not really
;; just about the tonality...
(defn make-tonality
  "Create a tonality that will be associated with a swarm."
  [& {:keys [scalef range durations synth] :or {scalef (comp C major)
                                                range  12
                                                durations    [1 2 2 4 4 8]
                                                synth  audio/tonal-sine}}]
  {:scalef scalef
   :range range
   :synth synth
   :durations durations})

;; Add swarms and music constraints, store as key in atom
(defn add-swarm!
  "
   searchmodes: :stationary :random :closest :farthest
  "
  [& {:keys [sid particles searchmode target weights tonality]}]
  (let [wx (first @state/world-size)
        wy (second @state/world-size)]
    (swap! state/tonalkeys assoc sid tonality)
    (swap! state/pso-state assoc sid 
           (pso/make-swarm :particles particles
                           :searchmode searchmode
                           :max-x (* wx @gfx/px-scaling)
                           :max-y (* wy @gfx/px-scaling)
                           :weights weights 
                           :target target))))

(defn remove-swarm!
  ""
  [k]
  (swap! state/tonalkeys dissoc k)
  (swap! state/pso-state dissoc k))

(defn update-swarm!
  ""
  [swarmid k uval]
  (let [sw (get @state/pso-state swarmid)]
    (swap! state/pso-state assoc swarmid (assoc sw k uval))))

(defn update-tonality!
  ""
  [swarmid k uval]
  (let [sw (get @state/tonalkeys swarmid)]
    (swap! state/tonalkeys assoc swarmid (assoc sw k uval))))
