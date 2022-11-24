(ns main)

(use '[overtone.live])


(use '[overtone.inst.synth])

(use '[overtone.sc.machinery.synthdef])


(odoc env-gen)


(demo

 (let [src (* 0.8     (sin-osc [440 440]))
       delay (delay-n src 0.4 0.4)]
   (+
    src delay
  ;; (* 0.2 (sin-osc [200 200]))
    )))

(demo
 (let [freq 440 amp 0.1 offset 7 delay 0.4]
   (let [src (* (env-gen (perc) :action FREE) (sin-osc [freq (+ offset freq)]))
         del (delay-n src delay delay)]
     (out 0 (* amp (+ src del))))))

(defsynth beep6 [freq 440 amp 0.1 offset 7 delay 0.4]
  (let [src (* (env-gen (perc) :action FREE) (sin-osc [freq (+ offset freq)]))
        del (delay-n src delay delay)]
    (out 0 (* amp (+ src del)))))

(let [env (asr)]
  (demo (sin-osc :freq (+ 200 (* 200 (env-gen env :action FREE))))))

(demo (* (env-gen (lin 0.1 1 1 0.25) :action FREE) (sin-osc)))
(demo (* 0.25 (linen (impulse 0) 0.1 1 1.9 :action FREE) (sin-osc)))

(demo  (let [dur 1
             env (sin-osc:kr (/ 1 (* 2 dur)))]
         (line:kr 0 1 dur :action FREE)
         (* env (saw 220))))

(demo (let [dur 1
            env (abs (lf-saw (/ 1 dur) :iphase -2 :mul 0.5 :add 1))]
        (line:kr 0 1 dur :action FREE)
        (* env (saw 220))))


(let [L (Math/pow 2 10)
      A 1]
  (mapv (fn [n] (Math/sin (/ (* 2 Math/PI n) L))) (range 0 L)))


(defn generate-sinewave-buffer [len]
  (let [wavetable-vector (mapv (fn [n] (Math/sin (/ (* 2 Math/PI n) len))) (range 0 len))
        empty-buffer     (buffer len)
        wavetable-buffer (buffer-write! empty-buffer wavetable-vector)]
    wavetable-buffer))

(generate-sinewave-buffer 1024)

(def our-sinewave-table (generate-sinewave-buffer 1024))


(demo (let [buf our-sinewave-table
            frequency 440
            table-size (buffer-size buf)
            current-sample-rate (sample-rate)
            playback-rate (* frequency (/ table-size current-sample-rate))]
        (play-buf:ar 1 buf :rate playback-rate :loop true :action FREE)))

(defsynth kick [amp 0.5 decay 0.6 freq 65]
  (let [env (env-gen (perc 0 decay) 1 1 0 1 FREE)
        snd (sin-osc freq (* Math/PI 0.5))]
    (out 0 (pan2 (* snd env amp) 0))))

(kick 0.9 0.5 80)


(after-delay 2500 (kick 0.9 0.5 80))

(show-schedule)

(doseq []
  (kick 0.9 0.2 100)
  (Thread/sleep 250)
  (kick 0.9 0.9 120)
  (Thread/sleep 250)
  (kick 0.9 0.2 125)
  (Thread/sleep 250)
  (kick 0.9 0.2 120)
  (Thread/sleep 250)
  (kick 0.9 0.2 100)
  (Thread/sleep 250)
  (kick 0.9 0.2 120))

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

(melody (vechord :g5 :minor7) 200)

(doseq []
  (melody (vechord :a3 :major7) 250)
  (melody (vechord :f3 :major7) 250)
  (melody (vechord :c4 :major7) 250)
  (melody (vechord :g4 :major7) 250)
  (melody (vechord :a3 :minor7) 250)
  (melody (vechord :f3 :major7) 250)
  (melody [48 52 55 0] 250)
  (melody [43 50 55 0] 250))
