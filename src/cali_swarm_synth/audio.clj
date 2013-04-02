(ns cali-swarm-synth.audio
  (:use [overtone.live]))


(def notes (atom {}))


(defn notebox
  [note min max]
  (nth (cycle (range min max)) note))

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
               (* 0.3 (sin-osc (midicps (- note 12)))))
        env (env-gen (perc 0.01 dur) :action FREE)]
    (* env snd amp)))

(definst kick
  [amp 0.8 dur 0.5]
  (let [signal (+ (* 0.8 (sin-osc 75)) 
                  (* 0.8 (square 50)))
        env (env-gen (perc 0.01 dur) :action FREE)]
    (* env signal amp)))


(defn all-stop
  []
  (stop))

(defn synth-ctl
  "Just a wrapper around overtones 'ctl' call."
  [s & args]
  (apply (partial ctl s) args))



(comment

(kick 0.8 1.0)

  ;;
  ;; Play some drums on the given interval:
  (let [dur 0.20]
    (doseq [ks [1 0 0 0 1 0 0 0 1 0 1 0 0 0 1]]
      (if (zero? ks) (Thread/sleep (* 1000 dur))
          (do
            (tonal 60)
            (kick :dur dur)))))


  (take 20
        (repeatedly 
         #(do
            (kick :dur (rand))
            (tonal 50 :amp 0.8)
            (Thread/sleep 500)
            )))



  (let [a (tdt-synth-env :freq 100 :filter 30 :amp 0.8 :pan-rate 0.24 :gate 1.0)]
    (ctl a :freq 50)
    (Thread/sleep 100)
    (ctl a :freq 100)
    (Thread/sleep 100)
    (ctl a :freq 200)
    (Thread/sleep 100)
    (ctl a :freq 300)
    )

  (def foo (tdt-synth-env :freq 70 :filter 20 :amp 0.8 :pan-rate 0.24 :gate 0.5))
  (def foo2 (tdt-synth-env :freq 90 :filter 20 :amp 0.8 :pan-rate 0.24 :gate 0.5))

  (ctl foo :gate 1)

  (ctl foo :freq 150)
  
  (ctl foo2 :freq 600)

  (ctl foo2 :filter 100)

  (ctl foo :pan-rate 0.10)

  (stop)


  )