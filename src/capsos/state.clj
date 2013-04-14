(ns capsos.state
  (:require [capsos.ca     :as ca]
            [capsos.pso    :as pso]
            [capsos.audio  :as audio]
            [overtone.live :as ot])
  (:use [leipzig.scale]))

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


(def ^:dynamic tonalkey (comp C sharp major low low))

(defn synth-event
  [state]
  (let [xsum (reduce #(+ %1 (first %2)) 0 state)
        ysum (reduce #(+ %1 (second %2)) 0 state)
        tone (mod (+ xsum ysum) 15)
        tone (tonalkey tone)
        dur  (* (count state) 0.35)]
    (when (and (pos? xsum) (pos? ysum))
      (audio/tonal tone 0.5 dur))))

(defn synth-event-free
  [state]
  (let [xsum (reduce #(+ %1 (first %2)) 0 state)
        ysum (reduce #(+ %1 (second %2)) 0 state)
        {ofrq :freq} (ot/node-get-control @synthatom [:freq])
        pfrq (* xsum ysum)
        pfrq (if (zero? pfrq) ofrq pfrq)
        pflt (* 10 (max 1 (count state)))]
    (println "Freq:" xsum ysum pfrq)
    (audio/synth-ctl @synthatom :amp 1.0 :freq pfrq :filter 30)
    ))

(defn timed-ca-state-update
  ""
  []
  ;;(reset! synthatom (audio/tdt-synth-env :freq 10 :filter 30 :amp 0.0 :pan-rate 0.24 :gate 1.0))
  
  (while @running?
    (if (not @paused?)
      (do
        (swap! world-state #(ca/step % :flat (not @toroidal?) :worldsize @world-size))
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
  ;;(reset! synthatom (audio/tdt-synth-env :freq 300 :filter 30 :amp 1.0 :pan-rate 0.24 :gate 1.0))
  
  (while @running?
    (if (not @paused?)
      (let [hits (quil-target-fn @pso-state @world-state)]
        (println "Hits:" hits)
        (synth-event hits)
        ))
    (Thread/sleep 250)))


(comment

  (ot/stop)

  (reset! pso-target-speed 200)

  (def ^:dynamic tonalkey (comp F sharp mixolydian low low))


  )