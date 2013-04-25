(ns capsos.core
  (:require [capsos.state :as state]
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
;; Add a command line main- here.
