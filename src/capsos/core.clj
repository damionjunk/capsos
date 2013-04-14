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
  (future (state/timed-pso-targeter    (partial gfx/pso-find-target :random)))
  (future (state/timed-pso-intersector gfx/pso-find-hits))
  (future (state/timed-pso-state-update))
  (future (state/timed-ca-state-update)))


(comment

  (go {:x 45 :y 30 :scalingpx 20 :cadelay 200 :psodelay 100 :particles 5
       :retarget-delay 200})

  (gfx/stop)

  (reset! state/world-state (ca/ca-glider-gun 0 10))

  )

(comment
  (swap! state/toroidal? not)

  (reset! state/world-state (ca/ca-glider 0 4))

  (swap! state/world-state union (ca/ca-glider 0 4))

  (swap! state/world-state union (ca/ca-blinker 4 4) (ca/ca-blinker 8 8))

  (reset! state/pso-state (pso/make-swarm
                           :target {:x 300 :y 200}
                           :particles 15 :max-x 500 :max-y 500))
  
  (dotimes [x 50]
    (swap!  state/pso-state pso/step))

  (reset! state/world-state #{})

  (swap! state/world-state union (ca/ca-blinker 4 4) (ca/ca-blinker 8 8))

  (reset! state/world-state (ca/ca-glider-gun 0 10))

  (reset! state/ca-speed 200)

  (go {:x 45 :y 30 :scalingpx 20 :cadelay 200 :psodelay 100 :particles 5})
  (gfx/stop)

  (swap! state/pso-state pso/re-target {:x (rand-int 500) :y (rand-int 500)})

  (reset! state/paused? true)
  (reset! state/paused? false)

  
  )
