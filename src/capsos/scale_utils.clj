(ns capsos.scale-utils
  (:use [leipzig.scale]))

(defn chord-arp
  "Given a key, mode, and chord map from Leipzig,
   N-octaves of the chord are returned as a MIDI note sequence.

   Example:

   (chord-arp (comp C major) seventh 2)

   ;; => (60 64 67 71 72 76 79 83)
   "
  [key-fn chord-map octaves]
  (let [s (map key-fn (range 12))
        v (map #(nth s %) (map second chord-map))]
    (sort (flatten (take octaves
                         (iterate (fn [ms] (map (partial + 12) ms)) v))))))

(defn mod-nth-chord
  [carp v]
  (nth carp (mod v (count carp))))
