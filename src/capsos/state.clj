(ns capsos.state
  (:require [capsos.ca     :as ca]
            [capsos.pso    :as pso]
            [capsos.audio  :as audio]
            [overtone.live :as ot]
            ))

(defonce world-state (atom #{}))
(defonce pso-state (atom {}))
(defonce paused?   (atom false))
(defonce ca-speed (atom 500))
(defonce pso-speed (atom 100))
(defonce pso-target-speed (atom 500))
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
      (do (swap! pso-state pso/step)))
    (Thread/sleep @pso-speed)))


(defn timed-pso-targeter
  "Requires a function that takes as first paramter a swarm, and as a second
   parameter a seq of CA grid positions. (not xy pixel coordinates).

   The function should return an XY-Grid position map"
  [target-fn]
  (while @running?
    (if (not @paused?)
      (let [target (target-fn @pso-state @world-state)]
        (println "Targeting:" target)
        (swap! pso-state pso/re-target target)))
    (Thread/sleep @pso-target-speed)))


(defn timed-pso-intersector
  ""
  [quil-target-fn]
  (while @running?
    (if (not @paused?)
      (let [hits (quil-target-fn @pso-state @world-state)]
        (println hits)
        ))
    (Thread/sleep 50)))