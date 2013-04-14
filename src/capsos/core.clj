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

  (go {:x 45 :y 30 :scalingpx 20 :cadelay 200 :psodelay 100 :particles 6
       :retarget-delay 2000})

  (gfx/stop)

  (reset! state/world-state (ca/ca-glider-gun 0 10))
  (reset! state/world-state #{})


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
