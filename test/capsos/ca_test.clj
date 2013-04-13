(ns capsos.ca-test
  (:use midje.sweet
        capsos.ca))

;;
;; ## Neighbor Tests in a 4x4 world-size
;;

(facts "[4 x 4] about flatland neighbors"
       (neighbors [0 0] :flat true :worldsize [4 4])
       => '([0 1] [1 0] [1 1])
       (neighbors [1 1] :flat true :worldsize [4 4])
       => '([0 0] [0 1] [0 2] [1 0] [1 2] [2 0] [2 1] [2 2])
       )

(facts "[4 x 4] about torus neighbors"
       (neighbors [0 0] :flat false :worldsize [4 4])
       => '([3 3] [3 0] [3 1] [0 3] [0 1] [1 3] [1 0] [1 1])
       (neighbors [1 1] :flat false :worldsize [4 4])
       => '([0 0] [0 1] [0 2] [1 0] [1 2] [2 0] [2 1] [2 2])
       )

 ;;
 ;; ## Oscillator Class
 
(facts "[4 x 4] about cell stepping: Blinker on Torus"
       (step #{[1 0] [1 1] [1 3]} :flat false :worldsize [4 4])
       => #{[1 0] [0 0] [2 0]} 
       (step #{[1 0] [0 0] [2 0]} :flat false :worldsize [4 4])
       => #{[1 0] [1 1] [1 3]})

(facts "[4 x 4] about cell stepping: Toad on Flat"
       (step #{[2 1] [2 2] [1 1] [1 2] [0 2] [3 1]} :flat true :worldsize [4 4])
       => #{[3 2] [0 1] [0 2] [1 3] [3 1] [2 0]}
       (step #{[3 2] [0 1] [0 2] [1 3] [3 1] [2 0]} :flat true :worldsize [4 4])
       => #{[2 1] [2 2] [1 1] [1 2] [0 2] [3 1]})
 




;;
;; ## Neighbor Tests in a 8x8 world-size
;;

(facts "[8 x 8] about flatland neighbors"
       (neighbors [0 0] :flat true :worldsize [8 8])
       => '([0 1] [1 0] [1 1])
       (neighbors [1 1] :flat true :worldsize [8 8])
       => '([0 0] [0 1] [0 2] [1 0] [1 2] [2 0] [2 1] [2 2])
       )
(facts "[8 x 8] about torus neighbors"
       (neighbors [0 0] :flat false :worldsize [8 8])
       => '([7 7] [7 0] [7 1] [0 7] [0 1] [1 7] [1 0] [1 1])
       (neighbors [1 1] :flat false :worldsize [8 8])
       => '([0 0] [0 1] [0 2] [1 0] [1 2] [2 0] [2 1] [2 2])
       )

(facts "[8 x 8] about cell stepping: Toad on Flat"
       (step #{[2 1] [2 2] [1 1] [1 2] [0 2] [3 1]} :flat true :worldsize [8 8])
       => #{[3 2] [0 1] [0 2] [1 3] [3 1] [2 0]}
       (step #{[3 2] [0 1] [0 2] [1 3] [3 1] [2 0]} :flat true :worldsize [8 8])
       => #{[2 1] [2 2] [1 1] [1 2] [0 2] [3 1]})

(facts "[8 x 8] about cell stepping: Toad on Torus"
       (step #{[2 1] [2 2] [1 1] [1 2] [0 2] [3 1]} :flat false :worldsize [8 8])
       => #{[3 2] [0 1] [0 2] [1 3] [3 1] [2 0]}
       (step #{[3 2] [0 1] [0 2] [1 3] [3 1] [2 0]} :flat false :worldsize [8 8])
       => #{[2 1] [2 2] [1 1] [1 2] [0 2] [3 1]})
 