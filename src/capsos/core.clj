(ns capsos.core
  (:use [clojure.set])
  (:require [capsos.state :as state]
            [capsos.ca    :as ca]
            [capsos.pso   :as pso]
            [capsos.quil  :as gfx]))


(defn go
  "When the main() is added for command line startup, this will be called
   and the command line options map will be passed as opts. Opts should match
   the keys defined in run-sketch if at all possible."
  [opts]
  (gfx/run-sketch opts)
  (future (state/timed-pso-targeter    gfx/pso-find-target))
  (future (state/timed-pso-intersector gfx/pso-find-hits))
  (future (state/timed-pso-state-update))
  (future (state/timed-ca-state-update)))


;;
;; ## REPL launch from here:
;;
;;    go, to start up quil / overtone
;;        add some CA elements by resetting 'world-state'

(comment

  (go {:x 45 :y 30 :scalingpx 20 :cadelay 200 :psodelay 100 
       :retarget-delay 2000})

  (gfx/stop)

  (reset! state/world-state (ca/ca-glider-gun 0 1))

  (reset! state/world-state (ca/ca-glider-gun 0 10))
  (reset! state/world-state (ca/ca-glider-gun 0 17))

  (reset! state/world-state #{})
  (reset! state/pso-state {})

  (swap! state/world-state union (ca/ca-glider-gun 0 5)
         (ca/ca-glider-gun 0 10)
         (ca/ca-glider-gun 0 15)
         (ca/ca-glider-gun 0 25))


  (gfx/add-swarm! :melody 12
             :random
             {:x 200 :y 200}
             {:c1 0.66, :c2 1.72, :nd 10, :vs 0.05} )

  (gfx/remove-swarm! :melody )

  (gfx/update-swarm! :bass :weights {:c1 0.06, :c2 2.72, :nd 10, :vs 0.05} )
  (gfx/update-swarm! :melody :weights {:c1 0.06, :c2 2.72, :nd 10, :vs 0.75} )

  (gfx/update-swarm! :bass :searchmode :closest)
  (gfx/update-swarm! :melody :searchmode :closest)

 

  ;;(swap! state/pso-state assoc :searchmode :stationary)

  )

(comment
  (swap! state/toroidal? not)

  (reset! state/world-state (ca/ca-glider 0 4))

  (swap! state/world-state union (ca/ca-glider 0 4))

  (swap! state/world-state union (ca/ca-blinker 4 4) (ca/ca-blinker 8 8))


  (swap! state/world-state union (ca/ca-blinker 4 4) (ca/ca-blinker 8 8))

  (reset! state/world-state (ca/ca-glider-gun 0 10))

  (reset! state/ca-speed 400)

  (go {:x 45 :y 30 :scalingpx 20 :cadelay 200 :psodelay 100 :particles 5})
  (gfx/stop)

  (reset! state/paused? true)
  (reset! state/paused? false)

  
  )
