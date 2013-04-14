(ns capsos.state
  (:require [capsos.ca     :as ca]
            [capsos.pso    :as pso]
            [capsos.audio  :as audio]
            [overtone.live :as ot])
  (:use [leipzig.scale]))

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


;; Define some scale constraints for the PSOs
(def tonalkeys (atom {1 {:scale (comp F sharp major low low)
                         :range 15
                         :durations [1 2 4 4 8]}
                      2 {:scale (comp F sharp major high)
                         :range 15
                         :durations [1 2 4 4 8]
                         }
                      }))

(def synthatom (atom nil))

;;
;; Right now this is using just a sum of the x,y to determine
;; pitch.
(defn synth-event
  [ca-state pso-k pso-st]
  (let [xsum    (reduce #(+ %1 (first %2)) 0 ca-state)
        ysum    (reduce #(+ %1 (second %2)) 0 ca-state)

        scalefn (get-in @tonalkeys [pso-k :scale])
        tonalr  (get-in @tonalkeys [pso-k :range])
        tonald  (get-in @tonalkeys [pso-k :durations])
        
        tone    (mod (+ xsum ysum) tonalr)
        tone    (scalefn tone)

        durp    (dec (min (count ca-state) (count tonald)))
        durm    (if (pos? durp) (nth tonald durp) 1)
        dur     (* durm 0.250)]
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
      (let [ks (keys @pso-state)]
        (doseq [k ks]
          (swap! pso-state
                 (fn [pso-s]
                   (assoc pso-s k (pso/step (get pso-s k))))))))
    (Thread/sleep @pso-speed)))


(defn timed-pso-targeter
  "Requires a function that takes as first paramter a swarm, and as a second
   parameter a seq of CA grid positions. (not xy pixel coordinates).

   The function should return an XY-Grid position map"
  [target-fn]
  (while @running?
    (if (not @paused?)
      (doseq [[k pso-s] @pso-state]
        (let [target (target-fn pso-s @world-state)]
          (println (format "PSO (%d) Targeting (%s): %s" k (:searchmode pso-s) target))
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
          (println (format "PSO (%s) Hits: %s" k (count hits)))
          (synth-event hits k (get @pso-state k)))))
    (Thread/sleep 250)))
