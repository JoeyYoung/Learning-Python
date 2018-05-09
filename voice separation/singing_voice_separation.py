def singing_voice_separation(X, fs, lmbda=1, nFFT=1024, gain=1, power=1):
    scf = 2 / 3.0
    S_mix = scf * librosa.core.stft(X, n_fft=nFFT)

    A_mag, E_mag = inexact_alm_rpca(np.power(np.abs(S_mix), power),
                                    lmbda=lmbda / math.sqrt(max(S_mix.shape)))
    PHASE = np.angle(S_mix)

    A = A_mag * np.exp(1j * PHASE)
    E = E_mag * np.exp(1j * PHASE)

    mask = np.abs(E) > (gain * np.abs(A))
    Emask = mask * S_mix
    Amask = S_mix - Emask

    wavoutE = librosa.core.istft(Emask)
    wavoutA = librosa.core.istft(Amask)

    wavoutE /= np.abs(wavoutE).max()
    wavoutA /= np.abs(wavoutA).max()

    return wavoutE, wavoutA



if __name__ == '__main__':
    if len(sys.argv) != 4:
        print('%s [input wav] [voice output] [music output]' % sys.argv[0])
        sys.exit(-1)

    data, sr = librosa.load(sys.argv[1])
    E, A = singing_voice_separation(data, sr)
    librosa.output.write_wav(sys.argv[2], E, sr)
librosa.output.write_wav(sys.argv[3], A, sr)