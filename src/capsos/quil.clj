(ns capsos.quil
  (:require [capsos.state :as state]
            [capsos.pso   :as pso])
  (:use [quil.core]
        [clojure.set]))

(defonce px-scaling (atom 50))
(defonce ^:dynamic pause-position [650 10 25])
(defonce ^:dynamic toroid-position [650 10 25])
(defonce ^:dynamic text-start-position [650 100])


;;
;; ## Some GUI Controls
;; 

(defn pause-button
  ""
  []
  (if @state/paused? (fill 60 75 200) (fill 60 75 10))
  (rect (first pause-position) (second pause-position)
        (last pause-position)  (last pause-position)))

(defn toroidal-button
  ""
  []
  (if @state/toroidal? (fill 60 75 200) (fill 60 75 10))
  (rect (first toroid-position) (second toroid-position)
        (last toroid-position)  (last toroid-position)))

(defn clicked-pause?
  [rx ry]
  (let [px (first  pause-position)
        py (second pause-position)
        pd (last   pause-position)]
    (and (>= rx px) (<= rx (+ pd px)) (>= ry py) (<= ry (+ py pd)))))

(defn clicked-toroid?
  [rx ry]
  (let [px (first  toroid-position)
        py (second toroid-position)
        pd (last   toroid-position)]
    (and (>= rx px) (<= rx (+ pd px)) (>= ry py) (<= ry (+ py pd)))))


;;
;; ## Drawing
;;

(defn draw-ca-state
  "Draws the squares on the screen!"
  [ca-state]
  (doseq [[x y] ca-state]
    (fill (* x (/ 255 (first @state/world-size))) 55 (* y (/ 255 (second @state/world-size))))
    (rect (* @px-scaling x) (* @px-scaling  y) @px-scaling @px-scaling)))

(defn draw-pso-state
  ""
  [pso-state]
  (let [particles (:particles pso-state)
        target    (:target pso-state)]
    (fill 100 255 255)
    (doseq [{x :x y :y} particles]
      (ellipse x y 8 8))
    (ellipse (:x target) (:y target) 10 10)))

(defn draw
  "Calls the Quil draw functions. @world-state is updated in another thread,
   'timed-state-update'."
  []
  (when @state/running?
    (background 0) ;; clear the background, then paint.
    (fill 200 200 200)
    (text (format "Live: %d\nToroidal? %s\nH: %d\nW: %d\nSpeed %d\nPaused: %s"
                  (count @state/world-state) @state/toroidal? (second @state/world-size)
                  (first @state/world-size) 
                  @state/ca-speed @state/paused?)
          (first text-start-position) (second text-start-position))
    (pause-button)
    (toroidal-button)
    (draw-ca-state @state/world-state)
    (draw-pso-state @state/pso-state)
    ))

(defn find-cell-pos
  "Given a real X or Y value from the screen, find the corresponding CA
   cell position."
  [coord]
  (/ (- coord (mod coord @px-scaling)) @px-scaling))

(defn in-world?
  [x y]
  (and (< x (first @state/world-size)) (< y (second @state/world-size))))


;;
;; Method to search though the seq of points, choosing the one that
;; fits the search criteria.

(defn tgt-min
  [targets curr]
  (apply min-key (partial pso/distance-squared curr) targets))

(defn tgt-max
  [targets curr]
  (apply max-key (partial pso/distance-squared curr) targets))

(defn to-xy-maps
  [caps]
  (map (fn [[x y]] {:x x :y y}) caps))

(defn to-xy-tuple [xy-map] [(:x xy-map) (:y xy-map)])


(defn pso-find-target
  "
   points: a seq of unscaled CA points.
   searchmode: :closest, :farthest, :random, also with found as a prefix
               to designate only jump when found.
  "
  [searchmode swarm points]
  ;; For now, lets just implement random
  (let [ctpx (:target swarm)
        ct {:x (find-cell-pos (:x ctpx))
            :y (find-cell-pos (:y ctpx))}
        xy (cond
            (= :random searchmode) (rand-nth (seq points))
            (= :closest searchmode) (to-xy-tuple (tgt-min (to-xy-maps points) ct))
            (= :farthest searchmode) (to-xy-tuple (tgt-max (to-xy-maps points) ct))
            )
        x  (* @px-scaling (first xy))
        y  (* @px-scaling (second xy))]
    {:x x :y y}))

(defn pso-find-hits
  "Find the cells of the CA that points in the PSO are touching."
  [swarm points]
  (->> (:particles swarm)
       (map (fn [{x :x y :y}]
              [(int (find-cell-pos x))
               (int (find-cell-pos y))]))
       (into #{})
       (intersection points)))

;;
;; ## Events
;;


(defn mouse-clicked
  []
  (let [button (mouse-button)
        x      (mouse-x)
        y      (mouse-y)
        ca-x   (find-cell-pos x) 
        ca-y   (find-cell-pos y)]
    (println x y ca-x ca-y button)
    ;; Handle in-world stuff
    (if (in-world? ca-x ca-y)
      (cond
       (= :left button)
       (let [pos [ca-x ca-y]]
         (if (get @state/world-state pos)
           (swap! state/world-state #(difference % #{pos}))
           (swap! state/world-state union #{pos})))
       (= :right button) (swap! state/world-state #(difference % #{[ca-x ca-y]})))
      ;; In controls / text
      (do
        (if (clicked-pause? x y) (swap! state/paused? not))
        (if (clicked-toroid? x y) (swap! state/toroidal? not))
        ))
    ))

;;
;; ## The Sketch
;;

(defn setup
  []
  (smooth)
  (frame-rate 24)
  (text-font (create-font "Inconsolata" 28))
  (background 0))

(defn run-sketch
  "Start the sketch, reset the atoms to the params listed"
  [{:keys [x y scalingpx cadelay psodelay particles retarget-delay]}]
  ;; Reset the state
  (reset! state/pso-state (pso/make-swarm :particles particles
                                          :max-x (* x scalingpx)
                                          :max-y (* y scalingpx)
                                          :target {:x 250 :y 250}))
  (reset! state/running? true)
  (reset! state/world-size [x y])
  (reset! state/ca-speed cadelay)
  (reset! state/pso-speed psodelay)
  (reset! state/pso-target-speed retarget-delay)
  (reset! px-scaling scalingpx)
  (def ^:dynamic pause-position  [(* x scalingpx) 10 25])
  (def ^:dynamic toroid-position [(+ 30 (* x scalingpx)) 10 25])
  (def ^:dynamic text-start-position [(* x scalingpx) 100])

  ;; Make the sketch
  (defsketch run-ca 
    :title "ca round one!"
    :setup setup
    :draw  draw
    :mouse-clicked mouse-clicked 
    :size [(+ 300 (* x scalingpx)) (* y scalingpx)]))

(defn stop
  "Stop and remove the sketch."
  []
  (reset! state/running? false)
  (sketch-stop run-ca)
  (sketch-close run-ca))
