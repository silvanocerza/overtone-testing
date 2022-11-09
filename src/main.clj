(ns main
  (:use [overtone.live]
        [overtone.inst.synth]))

(overpad 60)

(defn play [x]
  (if (seqable? x)
    (doseq [note x] (overpad note))
    (if (keyword? x)
      (overpad (note x))
      (overpad x))))

(defn melody [notes sleep]
  (if (empty? notes) nil
      (doseq []
        (play (first notes))
        (Thread/sleep sleep)
        (melody (rest notes) sleep))))

(defn vechord [x y]
  (conj (vec (chord x y)) 0))


(doseq []
  (melody (vechord :a3 :minor) 250)
  (melody (vechord :f3 :major) 250)
  (melody (vechord :c4 :major) 250)
  (melody (vechord :g4 :major) 250)
  (melody (vechord :a3 :minor) 250)
  (melody (vechord :f3 :major) 250)
  (melody [48 52 55 0] 250)
  (melody [43 50 55 0] 250))
