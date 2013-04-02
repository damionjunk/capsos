(ns cali-swarm-synth.pso-test
  (:use midje.sweet
        cali-swarm-synth.pso))


;; (defn update-particle-xy
;;   [gbest p]
;;   (let [pbest (:pbest p)
;;         nvelx (+ (* 2 (rand) (- (first pbest) (:x p)))
;;                  (* 2 (rand) (- (first gbest) (:x p))))
;;         nvely (+ (* 2 (rand) (- (second pbest) (:y p)))
;;                  (* 2 (rand) (- (second gbest) (:y p))))]
;;     (assoc p :x (+ nvelx (:x p)) :y (+ nvely (:y p)) :vel [nvelx nvely])))


(def ^:dynamic ^java.util.Random *rnd* (java.util.Random. 42)) 
;;(.nextDouble *rnd*)


(make-particle 1000 1000)
;; => {:x 740, :y 815, :pbest [740 815]}

(last (take 90000 (iterate (partial update-particle-xy [50 50]) {:x 740, :y 815, :pbest [740 815]})))

(update-particle-xy [50 50] {:x 740, :y 815, :pbest [740 815]})
{:norm-vel [-0.36547344376116647 -0.930821766991594], :vel [-473.25580940929353 -1205.3319229434064], :y 814.0691782330084, :x 739.6345265562388, :pbest [740 815]}


{:norm-vel [-0.4051418860917991 -0.9142538225974118], :vel [-633.3074826612985 -1429.138301368925], :y 649.0857461774026, :x 637.5948581139082, :pbest [1 1]}

;; (facts "Particle XY updating"
;;        (update-particle-xy [0 0] {:pbest [0 0] :x 1 y 1})
;;        => 
    
;;        )
