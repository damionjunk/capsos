(ns capsos.audio
  (:require [leipzig.scale :as l])
  (:use [overtone.live]
        [overtone.inst.drum]))

(defsynth tdt-synth-env [freq 440.0
                         amp 0.8
                         pan-rate 0.5
                         filter 7
                         gate 1.0]
  (let [pan-ug (sin-osc:kr (lin-lin:kr pan-rate 0 127 0.0 10.0))
        lfo-rate pan-ug
        lfo-pan pan-ug
        filter-f (lin-lin:kr filter 0 127 100 5000)
        freq-a (/ freq 2)               ; Octave below
        freq-b (/ (* (/ 3 2) freq) 2)   ; Octave Below, 5th above
        freq-c (* (/ 4 3) freq)         ; Perfect 4th
        basef-mod (lin-lin:kr (lf-saw:kr lfo-rate) -1.0 1.0 0 10)
        mud (lin-lin:kr (lf-noise0:kr 15) -1 1 -5 5)
                                        ; Adds a some wobble.
        basef freq                      ; c/p no mouse modification
        signal (+ (* 0.8 (sin-osc (+ basef freq-a basef-mod)))
                  (* 0.5 (square  (+ mud basef freq-b basef-mod)))
                  (* 0.7 (saw     (+ mud basef freq-c basef-mod))))
        ;env (env-gen (adsr 0.5 2 1 1) :gate (line:kr 1.0 0 1.0) :action FREE)
        env (env-gen (adsr 0.5 2 1 1) :gate gate :action FREE)
        ]
    (out 0 :signals (* env (pan2
                            (* amp (lpf:ar signal filter-f))
                            :pos (* 0.75 lfo-pan) ; Reduce Panning
                            )))))

(definst tonal
  [note 60 amp 0.3 dur 0.6]
  (let [snd (sin-osc (midicps note))
        snd (+ (* 0.9 snd)
               ;(* 0.3 (sin-osc (midicps (- note 12))))
               )
        env (env-gen (perc 0.01 dur) :action FREE)]
    (* env snd amp)))

(defn all-stop
  []
  (stop))

(defn synth-ctl
  "Just a wrapper around overtones 'ctl' call."
  [s & args]
  (apply (partial ctl s) args))

(comment

  ;;
  ;; Play some drums on the given interval:

  (dotimes [x 5]
    (let [slp 250
          nmp {:k [1 0 0 0  1 0 0 0  1 0 0 0  1 0 0 0]
               :s [0 0 0 0  0 0 0 0  0 0 0 0  0 0 0 0]
               :h [1 0 1 0  1 0 0 0  1 0 1 0  1 0 0 0]
               :o [0 0 0 0  0 0 1 0  0 0 0 0  0 0 1 0]}]
      (doseq [ctr (range (count (:k nmp)))]
        (let [k (nth (:k nmp) ctr)
              s (nth (:s nmp) ctr)
              h (nth (:h nmp) ctr)
              o (nth (:o nmp) ctr)]
          (do
            (if (pos? k) (kick       :amp 0.7))
            (if (pos? s) (snare      :amp 0.5))
            (if (pos? h) (closed-hat :amp 0.1))
            (if (pos? o) (open-hat   :amp 0.1))
            (Thread/sleep slp))))))


    (dotimes [x 4]
    (let [slp 250
          nmp {:k [1 1 0 0  1 0 0 0  1 0 0 1  1 0 0 0]
               :s [0 0 0 0  0 0 0 0  0 0 0 0  0 0 0 0]
               :h [1 0 1 1  1 0 1 1  1 0 1 0  1 0 0 0]
               :o [0 0 0 0  0 0 1 0  0 0 0 0  0 0 1 0]}]
      (doseq [ctr (range (count (:k nmp)))]
        (println (format "DrumSeq %d" (inc x)))
        (let [k (nth (:k nmp) ctr)
              s (nth (:s nmp) ctr)
              h (nth (:h nmp) ctr)
              o (nth (:o nmp) ctr)]
          (do
            (if (pos? k) (kick       :amp 0.7))
            (if (pos? s) (snare      :amp 0.5))
            (if (pos? h) (closed-hat :amp 0.1))
            (if (pos? o) (open-hat   :amp 0.1))
            (Thread/sleep slp))))))


  (stop)

  (closed-hat :amp 0.1)

  (snare :amp 1.0)
  (kick2 :amp 1.0)


  )