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
        env (env-gen (adsr 0.5 2 1 1) :gate (line:kr 1.0 0 1.0) :action FREE)
        ;env (env-gen (adsr 0.5 2 1 1) :gate gate :action FREE)
        ]
    (out 0 :signals (* env (pan2
                            (* amp (lpf:ar signal filter-f))
                            :pos (* 0.75 lfo-pan) ; Reduce Panning
                            )))))

(definst tonal-sine
  [note 60 amp 0.3 dur 0.6 filter 7]
  (let [filter-f (lin-lin:kr filter 0 127 100 5000)
        snd (sin-osc (midicps note))
        env (env-gen (perc 0.01 dur) :action FREE)]
    (* env (lpf:ar snd filter-f) amp)))

(definst tonal-square
  [note 60 amp 0.3 dur 0.6 filter 80]
  (let [filter-f (lin-lin:kr filter 0 127 100 5000)
        snd (square (midicps note))
        env (env-gen (perc 0.01 dur) :action FREE)]
    (* env (lpf:ar snd filter-f) amp)))

(definst tonal-square-autofiltered
  [note 60 amp 0.3 dur 0.6 filter 80]
  (let [;filter-f (lin-lin:kr (lf-saw:kr 100) 0 1.0 100 600)
        filter-f (lin-lin:kr (sin-osc:kr 0.05) -1 1 20 600)
        snd (square (midicps note))
        env (env-gen (perc 0.71 dur 1 4) :action FREE)]
    (* env (lpf:ar snd filter-f) amp)))


(defn all-stop
  []
  (stop))

(defn synth-ctl
  "Just a wrapper around overtones 'ctl' call."
  [s & args]
  (apply (partial ctl s) args))


(comment

  (tonal-square-autofiltered 57 0.8 9.0)
  (tonal-square 57 0.8 3.0 60)
  (tonal-sine 60 0.8 1.0 50)
  (tonal-sine 72 0.8 1.0 10)
  (stop)

  ;; Add Snare
  ;; Add Hi-Hat
  ;; Add Open Hi-Hat

  (closed-hat :amp 0.1)
  (open-hat :amp 0.1)
  (snare :amp 0.5)
  (kick :amp 7.0)


  

  (stop)
  )