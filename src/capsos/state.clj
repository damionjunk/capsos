(ns capsos.state
  (:require [capsos.ca     :as ca]
            [capsos.pso    :as pso]
            [capsos.audio  :as audio]
            [overtone.live :as ot]))

(defonce world-state (atom #{}))
(defonce paused?   (atom false))
(defonce ca-speed (atom 500))
(defonce pso-speed (atom 100))
(defonce pso-target-speed (atom 500))
(defonce world-size (atom [4 4]))
(defonce running? (atom true))
(defonce toroidal? (atom false))

;; Contains key->PSO data structs
(defonce pso-state (atom {}))
(def synthatom (atom nil))

;; Define some scale constraints for the PSOs
(def tonalkeys (atom {}))

(defn synth-event
  [ca-state pso-k pso-st]
  (when (pos? (count ca-state)) 
    (let [xmax    (apply max (map first ca-state))
          xmin    (apply min (map first ca-state))
          ymax    (apply max (map second ca-state))
          ymin    (apply min (map second ca-state))
          xbar    (/ (+ xmax xmin) 2) 
          ybar    (/ (+ ymax ymin) 2)

          scalefn (get-in @tonalkeys [pso-k :scalef])
          tonalr  (get-in @tonalkeys [pso-k :range])
          tonald  (get-in @tonalkeys [pso-k :durations])
          
          tone    (mod xbar tonalr)
          tone    (scalefn tone)

          fltr    (mod (* 2 ybar) 150)

          durp    (dec (min (count ca-state) (count tonald)))
          durm    (if (pos? durp) (nth tonald durp) 1)
          dur     (* durm 0.250)]
      ((get-in @tonalkeys [pso-k :synth]) tone 0.5 dur fltr))))

(defn synth-event-free
  [state]
  (let [xsum (reduce #(+ %1 (first %2)) 0 state)
        ysum (reduce #(+ %1 (second %2)) 0 state)
        {ofrq :freq} (ot/node-get-control @synthatom [:freq])
        pfrq (* xsum ysum)
        pfrq (if (zero? pfrq) ofrq pfrq)
        pflt (* 10 (max 1 (count state)))]
    ;;(println "Freq:" xsum ysum pfrq)
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
      (let [ks (keys @pso-state)]
        (doseq [k ks]
          (swap! pso-state
                 (fn [pso-s]
                   (assoc pso-s k (pso/step (get pso-s k))))))))
    (Thread/sleep @pso-speed)))


;;
;; This should *eventually* be moved to a per-swarm basis as well
(defn timed-pso-targeter
  "Requires a function that takes as first paramter a swarm, and as a second
   parameter a seq of CA grid positions. (not xy pixel coordinates).

   The function should return an XY-Grid position map"
  [target-fn]
  (while @running?
    (if (not @paused?)
      (doseq [[k pso-s] @pso-state]
        (let [target (target-fn pso-s @world-state)]
          ;;(println (format "PSO (%s) Targeting (%s): %s" k (:searchmode pso-s) target))
          (swap! pso-state
                 (fn [pso-s]
                   (assoc pso-s k (pso/re-target (get pso-s k) target)))))))
    (Thread/sleep @pso-target-speed)))

(defn timed-pso-intersector
  ""
  [quil-target-fn]
  ;;(reset! synthatom (audio/tdt-synth-env :freq 300 :filter 30 :amp 1.0 :pan-rate 0.24 :gate 1.0))
  
  (while @running?
    (if (not @paused?)
      (doseq [k (keys @pso-state)]
        (let [hits (quil-target-fn (get @pso-state k) @world-state)]
          ;;(println (format "PSO (%s) Hits: %s" k (count hits)))
          (try
            (synth-event hits k (get @pso-state k))
            (catch Exception e
              (.printStackTrace e))))))
    (Thread/sleep 250)))
