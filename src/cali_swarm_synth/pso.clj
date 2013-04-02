(ns cali-swarm-synth.pso)

;; Velocity is x speed and y speed

;; local info weighting
(def ^:dynamic *c1* 0.66)
;; global info weighting
(def ^:dynamic *c2* 1.72)
;; constant used for nearness scramble
(def ^:dynamic *nd* 10)
;; constant used for velocity scramble modifier
(def ^:dynamic *vs* 0.05)

;; Fitness Function
(defn distance-squared
  "Euclidean distance squared"
  [{tx :x ty :y} {x :x y :y}]
  (let [v1 (- x tx)
        v2 (- y ty)]
    (+ (* v1 v1) (* v2 v2))))

(defn make-particle
  [max-x max-y]
  (let [x (rand-int max-x)
        y (rand-int max-y)]
    {:x x
     :y y
     :pbest [x y]}))

(defn update-gbest-pbest
  [swarm]
  (let [ff     (:fitness-fn swarm)
        target (:target swarm)
        ;; Update the pbest
        parts  (map (fn [elem]
                      (let [pbest (:pbest elem)
                            cdist (ff target {:x (first pbest) :y (second pbest)})
                            ndist (ff target elem)
                            pbest (if (< ndist cdist) [(:x elem) (:y elem)] pbest)]
                        (assoc elem :pbest pbest)))
                    (:particles swarm))
        ;; Find the gbest
        pmin   (apply min-key (partial ff target) (:particles swarm))]
    (assoc swarm :gbest [(:x pmin) (:y pmin)] :particles parts)
    ))

(defn update-particle-xy
  "Need to determine if we want to use the normalized versions of the particle
   velocity. Probably not."
  [gbest p]
  (let [pbest (:pbest p)
        nvelx (+ (* *c1* (rand) (- (first pbest) (:x p)))
                 (* *c2* (rand) (- (first gbest) (:x p))))
        nvely (+ (* *c1* (rand) (- (second pbest) (:y p)))
                 (* *c2* (rand) (- (second gbest) (:y p))))
        norm  (Math/sqrt (+ (* nvelx nvelx) (* nvely nvely)))
        norm  (if (pos? norm) norm 1)
        norm-x (/ nvelx norm)
        norm-y (/ nvely norm)
        ;; We're tossing away the normalized value for now ... 
        norm-x nvelx
        norm-y nvely
        ]
    (assoc p
      :x (+ norm-x (:x p)) :y (+ norm-y (:y p))
      :vel [nvelx nvely]
      :norm-vel [(/ nvelx norm) (/ nvely norm)])))

;;
;; Is P too close to any other particle in swarm?
(defn gb-nearness
  [swarmparts p]
  (> (count
      (filter #(< % *nd*)
              (map (partial distance-squared p) swarmparts)))
     1))

(defn rand-sign [] (if (> (rand) 0.5) 1 -1))

(defn vscrambler
  ""
  [vmod p]
  (let [x  (:x p)
        y  (:y p)
        dx (* (rand-sign) vmod (rand) x)
        dy (* (rand-sign) vmod (rand) y)]
    (assoc p :x (+ x dx) :y (+ y dy))))

;;
;; This isn't really part of the PSO algo
;;
(defn particle-scatter
  "If particles are too close to eachother, add some scatter
   so that we do not get behavior that's locked in on the x or
   y axis."
  [particles]
  (let [pgroups    (group-by (partial gb-nearness particles) particles)
        trues      (map (partial vscrambler *vs*) (get pgroups true))
        falses     (get pgroups false)
        nparticles (concat trues falses)]
    nparticles))


(defn make-swarm
  [& {:keys [particles max-x max-y target]}]
  (let [parts  (take particles (repeatedly #(make-particle max-x max-y)))
        smap   {:particles  parts
                :fitness-fn distance-squared
                :target     target}]
    (update-gbest-pbest smap)))

(defn off-board?
  [p b-x b-y]
  (let [x (:x p)
        y (:y p)]
    (or (neg? x) (neg? y) (> x b-x) (> y b-y))))



(defn step
  [swarm]
  (let [gbest (:gbest swarm)
       ;; update the x and y
        parts  (:particles swarm)
        parts  (particle-scatter parts)
        parts  (map (partial update-particle-xy gbest) parts)
        swarm  (assoc swarm :particles parts)
        swarm (update-gbest-pbest swarm)]
    swarm
    ))

(defn re-target
  "When the target moves, the gBest and pBest data is no longer valid."
  [swarm new-target]
  ;; Reset pbest, gbest, change target
  (let [parts (map (fn [part]
                     (assoc part :pbest [(:x part) (:y part)]))
                   (:particles swarm))]
    (update-gbest-pbest (assoc swarm :particles parts :target new-target))))

;;
;; ## Test at the REPL stuff.
;;

(comment

  (step sw)

  (def sw (make-swarm :target {:x 250 :y 250} :particles 5 :max-x 500 :max-y 500))
  (clojure.pprint/pprint sw)
  (last (take 7 (iterate step sw)))

  )