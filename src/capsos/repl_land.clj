(ns capsos.repl-land
  (:require [capsos.core        :as core]
            [capsos.ca          :as ca]
            [capsos.quil        :as gfx]
            [capsos.audio       :as audio]
            [capsos.state       :as state]
            [capsos.scale-utils :as su]
            [capsos.musistate   :as ms])
  (:use [leipzig.scale]
        [leipzig.chord]
        [clojure.set]))

;;
;; ## System Startup and Shutdown
;;

(comment

  (core/go {:x 35 :y 25 :scalingpx 20 :cadelay 200 :psodelay 100 
            :retarget-delay 2000})

  (gfx/stop)

  )


;;
;; ## Demo Part 1
;;

(comment

  ;; Blinker
  (reset! state/world-state (ca/ca-blinker 4 4))

  ;; Square
  (reset! state/world-state (ca/ca-square 4 4))

  ;; Glider
  (reset! state/world-state (ca/ca-glider 4 4))
  
  ;; Random

  ;; rstep 1 - a random integer

  ;; rstep 2 - some random integers

  ;; rstep 3 - we need X & Y!

  ;; rstep 4 - Only the unique items (plus CA is hashset of coordinates)

  ;; rstep 5 - I want a random number of random coordinates

  ;; rstep 6 - pretty picture time!

  ;; Keep the Elements

  ;; Inspect the world state, Pause inspect, etc.
  (reset! state/world-state #{})

  ;; Dump the state
  state/world-state

  )

;;
;; ## Demo Part 2
;;

(comment
  (core/go {:x 35 :y 25 :scalingpx 20 :cadelay 200 :psodelay 100 
            :retarget-delay 200})

  (gfx/stop)


  ;; pstep 1 - Add Glider, set Toroidal
  (swap!  state/toroidal? not)

  (swap! state/paused? not)

  (reset! state/world-state #{})

  (reset! state/world-state (ca/ca-glider 0 0))

  ;; pstep 2 - PSO Chase

  (ms/add-swarm! :sid        :yodawg
                 :particles  12
                 :searchmode :closest
                 :target     {:x 10 :y 15} ;; Set to anything initially
                 :weights    {:c1 0.66, :c2 1.72, :nd 10, :vs 0.75}
                 :tonality   (ms/make-tonality :scalef   (comp G sharp major)
                                               :range  6
                                               :durations    [2 2 4 4 8]
                                               :synth  audio/tonal-sine))


  (ms/remove-swarm! :yodawg)
  (ms/update-swarm! :yodawg :searchmode :random)
  (ms/update-tonality! :yodawg :range 12)
  (ms/update-tonality! :yodawg :durations [1 1])
  (ms/update-tonality! :yodawg :scalef (comp B flat pentatonic low))
  (ms/update-tonality! :yodawg :scalef (comp G sharp mixolydian))

  ;; pstep 3 - Another Glider and PSO

  (swap! state/paused? not)

  ;; New Gliders!
  (reset! state/world-state (ca/ca-glider 0 0))
  (swap! state/world-state union (ca/ca-glider 10 0))

  (ms/add-swarm! :sid        :lolcat
                 :particles  12
                 :searchmode :closest
                 :target     {:x 250 :y 15}
                 :weights    {:c1 0.66, :c2 1.72, :nd 10, :vs 0.05}
                 :tonality   (ms/make-tonality :scalef (comp G sharp mixolydian low low)
                                               :range 6
                                               :durations [2 2 2]
                                               :synth audio/tonal-square))
  (ms/remove-swarm! :lolcat)

  
  )


;;
;; ## Demo Part 3
;;


(comment

  (core/go {:x 36 :y 25 :scalingpx 20 :cadelay 200 :psodelay 100 
            :retarget-delay 200})
  
  (gfx/stop)

  (swap! state/toroidal? not)
  (swap! state/paused? not)

  (reset! state/world-state (ca/ca-glider-gun 0 1))

  (reset! state/world-state #{})
  (reset! state/pso-state {})
  (reset! state/tonalkeys {})

  (swap! state/world-state union
         (ca/ca-glider-gun 0 5)
         (ca/ca-glider-gun 0 15))

  ;; chord arp
  ;; (partial mod-nth-chord (chord-arp (comp G sharp aeolian low low low) seventh 2))

  ;;
  ;; Jake The Dog

  (ms/add-swarm! :sid        :jakethedog
                 :particles  8 
                 :searchmode :stationary
                 :target     {:x 200 :y 200}
                 :weights    {:c1 0.66, :c2 1.72, :nd 10, :vs 0.75}
                 :tonality  (ms/make-tonality :scalef (comp F chromatic low low low)
                                              :range 6 
                                              :durations [2 2 8]
                                              :synth audio/tonal-square))
  (ms/update-swarm! :jakethedog :weights
                    {:c1 0.06, :c2 1.72, :nd 10, :vs 1.05} )

  (ms/remove-swarm! :jakethedog)

  ;;
  ;; Finn The Human

  (ms/add-swarm! :sid        :finnthehuman
                 :particles  12
                 :searchmode :stationary
                 :target     {:x 650 :y 400}
                 :weights    {:c1 0.66, :c2 1.72, :nd 10, :vs 0.35}
                 :tonality   (ms/make-tonality :scalef (comp G sharp mixolydian)
                                               :range 8
                                               :durations [8 8]
                                               :synth audio/tonal-sine))
  (ms/remove-swarm! :finnthehuman)

  (ms/update-swarm! :finnthehuman :weights {:c1 0.66, :c2 1.72, :nd 10, :vs 0.75})
  (ms/update-swarm! :jakethedog   :weights {:c1 0.66, :c2 1.72, :nd 10, :vs 0.75})

  (ms/update-tonality! :finnthehuman
                       :scale (partial su/mod-nth-chord (su/chord-arp (comp G minor) seventh 2)))


)



(comment

  ;; Some TDT/Synth/Env Throwdown
  ;; :jakethedog
  ;; :finnthehuman

)