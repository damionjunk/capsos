(ns capsos.ca)

;;
;; ## CA Lifeforms
;;

(def origin-glider  #{[1 0] [2 1] [0 2] [1 2] [2 2]})
(def origin-blinker #{[1 0] [1 1] [1 2]})
(def origin-glider-gun #{[35 2] [35 3] [34 2] [34 3] [1 4] [0 4] [1 5] [0 5]
                         [24 0] [24 1] [22 1] [21 2] [24 5] [20 2] [21 3]
                         [24 6] [20 3] [21 4] [22 5] [20 4] [15 3] [16 4]
                         [17 5] [13 2] [16 5] [12 2] [16 6] [14 5] [11 3]
                         [15 7] [10 4] [10 5] [13 8] [10 6] [11 7] [12 8]})



(defn ca-shape
  "Provides a set representing the CA at point x,y"
  [x y ca]
  (into #{} (map (fn [[x1 y1]] [(+ x x1) (+ y y1)]) ca)))

(defn ca-glider
  "A Glider."
  [x y]
  (ca-shape x y origin-glider))

(defn ca-blinker
  "A Blinker."
  [x y]
  (ca-shape x y origin-blinker))

(defn ca-glider-gun
  "Glider gun!"
  [x y]
  (ca-shape x y origin-glider-gun))

;;
;; ## Cellular Automata Implementation
;;

(defn torus
  "Connect the sequence of coordinates as a torus instead of just a flat
   plane."
  [[x y] wsize]
  [(if (neg? x) (dec (first wsize))
       (if (>= x (first wsize)) 0 x))
   (if (neg? y) (dec (second wsize))
       (if (>= y (second wsize)) 0 y))])

(defn tor-or-bound
  "Torus or Bounded Flat"
  [[x y] & {:keys [flat worldsize] :or {flat true}}]
  (if flat
    (let [x (if (and (not (neg? x)) (< x (first worldsize))) x)
          y (if (and (not (neg? y)) (< y (second worldsize))) y)]
        (if (and x y) [x y]))
      (torus [x y] worldsize)))

(defn neighbors
  "Given the world described in the atom 'world-size', this function provides
   a sequence of neighboring cells to check for cell life."
  [[x y] & {:keys [flat worldsize] :or {flat true}}]
  (keep #(tor-or-bound % :worldsize worldsize :flat flat)
        (for [dx [-1 0 1] dy [-1 0 1] :when (not= 0 dx dy)]
          [(+ dx x) (+ dy y)])))

(defn step
  "Takes the current game state in cells, and provides the next one.
   Defaults to a flat board, but toroidal boards are also available by
   specifying the ':flat false' key and value."
  [cells & {:keys [flat worldsize] :or {flat true}}]
  (set (for [[loc n] (frequencies (mapcat #(neighbors % :worldsize worldsize :flat flat) cells))
             :when (or (= n 3) (and (= n 2) (cells loc)))]
         loc)))

