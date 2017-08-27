package com.amigohackson.jarvisdemo.utils;

import java.io.File;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;

public class AudioRecordUtil {
    private static final String TAG = "AudioRecord";
    private static final String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().getPath() +
            "/audio-sample.pcm";
    private static final int RECORDER_SAMPLE_RATE = 44100; // For real device (use 8000 for emulator)
    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    private int bufferSize;
    private AudioRecord audioRecorder;
    private boolean isRecording;
    private static AudioRecordUtil mInstance;

    // Exists for Singleton
    private AudioRecordUtil() {
        bufferSize = AudioRecord.getMinBufferSize(
                RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS_IN,
                RECORDER_AUDIO_ENCODING);
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR_INVALID_OPERATION) {
            Log.e(TAG, "Bad value for \"bufferSize\", recording parameters are " +
                    "not supported bye the hardware");
            Log.e(TAG, "\"bufferSize\"=" + bufferSize);
        }

        isRecording = false;
    }

    /**
     * Singleton method.
     *
     * @return mInstance
     */
    public static AudioRecordUtil getInstance() {
        if (mInstance == null)
            mInstance = new AudioRecordUtil();
        return mInstance;
    }

    public void startRecording() {
        deleteOldFiles();
        audioRecorder = new AudioRecord(
                RECORDER_AUDIO_SOURCE,
                RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS_IN,
                RECORDER_AUDIO_ENCODING,
                bufferSize);

        audioRecorder.startRecording();
        isRecording = true;
        Thread recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioData();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
        Log.d(TAG, "Starting recording!");
    }

    public void stopRecording() {
        if (audioRecorder != null) {
            isRecording = false;
            audioRecorder.stop();
            audioRecorder.release();
        }
        Log.d(TAG, "Stop recording");
    }

    private synchronized void writeAudioData(){
        File dataFile = new File(AUDIO_FILE_PATH);
        byte audioData[] = new byte[bufferSize];
        while (isRecording){
            audioRecorder.read(audioData, 0, bufferSize);
            try{
                FileUtils.writeByteArrayToFile(dataFile, audioData, true);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Writing audio Data");
    }

    public void playAudio(){
        byte[] byteData;
        try{
            byteData = FileUtils.readFileToByteArray(new File(AUDIO_FILE_PATH));
            Log.d(TAG, "File length: " + byteData.length+ " bytes");
        }catch (IOException e){
            Log.d(TAG, "Unable to read pcm file from" + AUDIO_FILE_PATH);
            e.printStackTrace();
            return;
        }

        // Set and push to audio track..
        int intSize = AudioTrack.getMinBufferSize(
                RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS_OUT,
                RECORDER_AUDIO_ENCODING);

        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS_OUT,
                RECORDER_AUDIO_ENCODING,
                intSize,
                AudioTrack.MODE_STREAM);

        audioTrack.play();
        // Write the byte array to the track
        audioTrack.write(byteData, 0, byteData.length);
        Log.d(TAG, "Audio has been played");
        audioTrack.stop();
        audioTrack.release();
    }

    // Wave Format: http://soundfile.sapp.org/doc/WaveFormat/
    public void transcodeRawToWav(){
        pcmToWave(RECORDER_SAMPLE_RATE, 1, 16);
    }

    private void pcmToWave(int sampleRate, int numChannels, int bitPerSample){
        byte[] byteData;
        try{
            byteData = FileUtils.readFileToByteArray(new File(AUDIO_FILE_PATH));
            Log.d(TAG, "File length: " + byteData.length+ " bytes");
        }catch (IOException e){
            Log.d(TAG, "Unable to read pcm file from" + AUDIO_FILE_PATH);
            e.printStackTrace();
            return;
        }

        byte[] header = new byte[44];
        long totalDataLen = byteData.length + 36;
        long byteRate = sampleRate * numChannels * bitPerSample / 8;

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte)numChannels;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) ((byteRate) & 0xff);
        header[29] = (byte) (((byteRate) >> 8) & 0xff);
        header[30] = (byte) (((byteRate) >> 16) & 0xff);
        header[31] = (byte) (((byteRate) >> 24) & 0xff);
        header[32] = (byte) ((numChannels * bitPerSample) / 8);
        header[33] = 0;
        header[34] = (byte)bitPerSample;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (byteData.length  & 0xff);
        header[41] = (byte) ((byteData.length >> 8) & 0xff);
        header[42] = (byte) ((byteData.length >> 16) & 0xff);
        header[43] = (byte) ((byteData.length >> 24) & 0xff);

        File waveFile = new File(AUDIO_FILE_PATH.replaceAll(".pcm", ".wav"));
        try{
            FileUtils.writeByteArrayToFile(waveFile,header,true);
            FileUtils.writeByteArrayToFile(waveFile,byteData, true);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void deleteOldFiles() {
        File dataFile = new File(AUDIO_FILE_PATH);
        FileUtils.deleteQuietly(dataFile);

        File waveFile = new File(AUDIO_FILE_PATH.replaceAll(".pcm", ".wav"));
        FileUtils.deleteQuietly(waveFile);
    }
}
