(ns exercise
  (:use overtone.core))

(boot-server)
(kill-server)

(defsynth foo [freq 200 dur 0.5]
  (let [src (saw [freq (* freq 1.01) (* 0.99 freq)])
        low (sin-osc (/ freq 2))
        filt (lpf src (line:kr (* 10 freq) freq 10))
        env (env-gen (perc 0.1 dur) :action FREE)]
    (out 0 (pan2 (* 0.8 low env filt)))))


(foo 440)

(defn foo-pause
  []
  (dotimes [i 10]
    (foo (* i 220) 1)
    (Thread/sleep 300)))

(foo-pause)

(defn foo-timed
  []
  (let [n (now)]
    (dotimes [i 10]
      (at (+ n (* i 300))
          (foo (* i 220) 1)))))

(foo-timed)

(definst overpad [note 60 amp 0.7 attack 0.001 release 2]
  (let [freq  (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        f-env (+ freq (* 3 freq (env-gen (perc 0.012 (- release 0.1)))))
        bfreq (/ freq 2)
        sig   (apply +
                     (concat (* 0.7 (sin-osc [bfreq (* 0.99 bfreq)]))
                             (lpf (saw [freq (* freq 1.01)]) f-env)))]
    (* amp env sig)))

(overpad 41 :attack 10 :release 20)

(let [n (now)]
  (dotimes [i 10]
    (at (+ n (* i 300))
        (ctl overpad :note (+ 41 (* i 0.9))))))

(stop)

(def metro (metronome 128))

(definst kick []
  (let [src (sin-osc 80)
        env (env-gen (perc 0.001 0.3) :action FREE)]
    (* 0.7 src env)))

(kick)

(defn player [beat notes]
  (let [notes (if (empty? notes)
                [50 55 53 50]
                notes)]
    (at (metro beat)
        (kick))
    (at (metro beat)
        (if (zero? (mod beat 5))
          (overpad (+ 24 (choose notes)) 0.2 0.75 0.005)))
    (at (metro (+ 0.5 beat))
        (if (zero? (mod beat 6))
          (overpad (+ 12 (choose notes)) 0.5 0.15 0.1)
          (overpad (choose notes) 0.5 0.15 0.1)))
    (apply-by (metro (inc beat)) #'player (inc beat) (next notes) [])))

(player (metro) [])
(stop)

(defn play-notes [t beat-dur notes attacks]
  (when notes
    (let [note (= 12 (first notes))
          attack (first attacks)
          amp 0.9
          release 0.1
          next-beat (+ t beat-dur)]
      (at t (overpad note amp attack release))
      (apply-by next-beat #'play-notes next-beat beat-dur (next notes) (next attacks) []))))

(play-notes (now) 425 (cycle [40 42 44 45 47 49 51 52]) (repeat 0.4))
(play-notes (now) 300 (scale :c4 :major) (repeat 0.05))
(play-notes (now) 300 (take 15 (cycle [40 42 44 45 47 49 51 52])) (repeat 0.3))
(play-notes (now) 100 (take 50 (cycle (scale :a4 :minor))) (repeat 0.4))
(stop)


(defn chord-notes []
  [(choose [58 60 60 62])
   (choose [62 63 63 65])
   (choose [65 67 68 70])])

(def metro (metronome 70))

(defn play-chords [b]
  (let [tick (* 2 (choose [125 500 250 250 500 250 500 250]))
        next-beat (inc b)]
    (at (metro b)
        (doseq   [note (map #(- % 12) (chord-notes))]
          (overpad note 0.3 (/ tick 1020))))
    (apply-by (metro next-beat) #'play-chords [next-beat])))


(play-chords (metro))
(metro-bpm metro 90)
(stop)


(defn looper [t dur notes]
  (at t (kick))
  (at (+ t 350) (doseq [note (chord-notes)] (overpad (first notes) 0.3 0.1)))
  (at t (overpad (- (first notes) 36) 0.3 (/ dur 1000)))
  (apply-by (+ t dur) #'looper (+ t dur) dur (next notes) []))

(looper (now) 500 (cycle [60 67 65 72 75 80]))

(defsynth pedestrian-crossing
  "Street crossing in Britain."
  [out-bus 0]
  (out out-bus (pan2 (* 0.2 (sin-osc 2500) (lf-pulse 5)))))

(pedestrian-crossing)
(stop)

(definst trancy-waves []
  (* 0.2
     (+ (sin-osc 200) (saw 200) (saw 203) (sin-osc 400))))

(trancy-waves)
(stop)

(demo 10 (bpf (* [0.5 0.5] (pink-noise))
              (mouse-y 10 10000)
              (mouse-x 0.0001 0.9999)))

(odoc lpf)
(defsynth roaming-sines
  []
  (let [freqs (take 5 (repeatedly #(ranged-rand 40 2000)))
        ampmod [(+ 1 (lpf :in 0 :freq 1)) (- (lpf :in 0 :freq 2) 1)]
        snd (splay (* 0.5 (sin-osc freqs)))
        snd (* (sin-osc ampmod) snd)]
    (out 0 snd)))

(roaming-sines)
(stop)