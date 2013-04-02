(ns cali-swarm-synth.state
  (:require [cali-swarm-synth.ca    :as ca]
            [cali-swarm-synth.pso   :as pso]
            [cali-swarm-synth.audio :as audio]
            [overtone.live          :as ot]
            ))

;; TODO: Add an atom for world-state metadata map
;;       can control 'heat-map' like coloration,
;;       note longivity, in drone zone
;;       (can make up concept of drone zone, if we want a gated
;;       continuous synth runing with params sent from that zoe)

(defonce world-state (atom #{}))
(defonce pso-state (atom {}))
(defonce paused?   (atom false))
(defonce ca-speed (atom 500))
(defonce pso-speed (atom 100))
(defonce world-size (atom [4 4]))
(defonce running? (atom true))
(defonce toroidal? (atom false))

(def synthatom (atom nil))




(defn synth-event
  [state]
  (let [xsum (reduce #(+ %1 (first %2)) 0 state)
        ysum (reduce #(+ %1 (second %2)) 0 state)]
    ;;(println "Freq:" xsum ysum)
    ;;(audio/synth-ctl @synthatom :amp 0.5 :freq (ot/midicps 60) :filter (rand-int ysum))
    ;;(audio/tonal xsum 0.4 (* 0.05 (/ xsum 2)))
    (if (and (pos? xsum) (pos? ysum))
      (audio/tonal (audio/notebox ysum 20 90) 0.7))
    ))

(defn timed-ca-state-update
  ""
  []
  ;;(reset! synthatom (audio/tdt-synth-env :freq 10 :filter 30 :amp 0.0 :pan-rate 0.24 :gate 1.0))
  
  (while @running?
    (if (not @paused?)
      (do
        (swap! world-state #(ca/step % :flat (not @toroidal?) :worldsize @world-size))
        (synth-event @world-state)
        ))
    (Thread/sleep @ca-speed)))


(defn timed-pso-state-update
  ""
  []
  (while @running?
    (if (not @paused?)
      (do
        (swap! pso-state pso/step)
        ))
    (Thread/sleep @pso-speed)))