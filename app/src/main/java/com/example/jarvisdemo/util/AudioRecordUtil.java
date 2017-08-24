package com.example.jarvisdemo.util;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

public class AudioRecordUtil {

	private static final String TAG = "AudioRecordUtil";
	private AudioRecord audioRecorder;
	private DataOutputStream outputStream;
	private Thread recordThread;
	private boolean isStart = false;
	private static AudioRecordUtil mInstance;
	private static int bufferSize;
	private File audioPcmFile, audioWavFile;
	
	public AudioRecordUtil() {
		bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		audioRecorder = new AudioRecord(AudioSource.MIC, 
										8000, 
										AudioFormat.CHANNEL_IN_MONO, 
										AudioFormat.ENCODING_PCM_16BIT, 
										bufferSize * 2);
	}
	
	/**
	 * Singleton method. 
	 * @return mInstance
	 */
	public synchronized static AudioRecordUtil getInstance() {
		if (mInstance == null) 
			mInstance = new AudioRecordUtil();
		return mInstance;
	}
	
	Runnable recordRunnable = new Runnable() {
		
		@Override
		public void run() {
			try {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
				int bytesRecord;
				byte[] tempBuffer = new byte[bufferSize];
				if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
					stopRecord();
					return;
				}
				audioRecorder.startRecording();
				Log.d(TAG, "Start recording. ");
				while (isStart) {
					if (audioRecorder != null) {
						bytesRecord = audioRecorder.read(tempBuffer, 0, tempBuffer.length);
						if (bytesRecord == AudioRecord.ERROR_INVALID_OPERATION || bytesRecord == AudioRecord.ERROR_BAD_VALUE) 
							continue;
						else if (bytesRecord != 0 && bytesRecord != -1) {
							// write audio file
							outputStream.write(tempBuffer, 0, bytesRecord);
						} else 
							break;
					}
				}
				transcodeRawToWav();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, e.getMessage());
			}
		}
	};
	
	private void setPath() throws Exception {
		File fPath = new File(Environment.getExternalStorageDirectory() + "/msc/");
		if (!fPath.exists()) {
			fPath.mkdirs();
		}
		audioPcmFile = File.createTempFile(String.format("record_%s", System.currentTimeMillis()), ".pcm", fPath);
		if (outputStream == null) {
			outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioPcmFile)));			
		}
	}
	
	private void startRecordThread() {
		destroyRecordThread();
		isStart = true;
		if (recordThread == null) {
			recordThread = new Thread(recordRunnable);
			recordThread.start();
		}
	}
	
	private void destroyRecordThread() {
		try {
			isStart = false;
			if (recordThread != null && recordThread.getState() == Thread.State.RUNNABLE) {
				try {
					Thread.sleep(500);
					recordThread.interrupt();
				} catch (Exception e) {
					recordThread = null;
				}
			}
			recordThread = null;
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		} finally {
			recordThread = null;
		}
	}
	
	public void startRecord() {
		try {
			setPath();
			startRecordThread();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
	}
	

	public void stopRecord() {
		try {
			isStart = false;
			destroyRecordThread();
			if (audioRecorder != null) {
				if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
					audioRecorder.stop();
				}
				audioRecorder.release();
			}
			if (outputStream != null) {
				outputStream.flush();
				outputStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
	}
	
	private void transcodeRawToWav() throws IOException {
		byte[] rawData = new byte[(int) audioPcmFile.length()];
		DataInputStream inputStream = null;
		try {
			inputStream = new DataInputStream(new FileInputStream(audioPcmFile));
			inputStream.read(rawData);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		
		DataOutputStream wavOutputStream = null;
		File fPath = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/msc/");
		fPath.mkdirs();
		if (fPath.exists()) {
			fPath.delete();
			audioWavFile = File.createTempFile(String.format("record_%s", System.currentTimeMillis()), ".wav", fPath);
		}
		try {
			wavOutputStream = new DataOutputStream(new FileOutputStream(audioWavFile));
			// add .wav file header
			writeString(wavOutputStream, "RIFF");
			writeInt(wavOutputStream, 36 + rawData.length);
			writeString(wavOutputStream, "WAVE");
			writeString(wavOutputStream, "fmt ");
			writeInt(wavOutputStream, 16);
			writeShort(wavOutputStream, (short) 1);
			writeShort(wavOutputStream, (short) 1);
			writeInt(wavOutputStream, 44100);
			writeInt(wavOutputStream, 44100 * 2);
			writeShort(wavOutputStream, (short) 2);
			writeShort(wavOutputStream, (short) 16);
			writeString(wavOutputStream, "data");
			writeInt(wavOutputStream, rawData.length);
			
			short[] shorts = new short[rawData.length / 2];
			ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
			ByteBuffer byteBuffer = ByteBuffer.allocate(shorts.length * 2);
			for (short s : shorts) {
				byteBuffer.putShort(s);
			}
			wavOutputStream.write(readFileToBytes(audioPcmFile));
			
		} finally {
			if (wavOutputStream != null) {
				wavOutputStream.close();
			}
		}
	}
	
	private byte[] readFileToBytes(File file) throws IOException {
	    int size = (int) file.length();
	    byte bytes[] = new byte[size];
	    byte tmpBuff[] = new byte[size];
	    FileInputStream fis= new FileInputStream(file);
	    try {
	        int read = fis.read(bytes, 0, size);
	        if (read < size) {
	            int remain = size - read;
	            while (remain > 0) {
	                read = fis.read(tmpBuff, 0, remain);
	                System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
	                remain -= read;
	            } 
	        } 
	    } catch (IOException e){
	        throw e;
	    } finally { 
	        fis.close();
	    }
	    return bytes;
	}
	
	private void writeInt(final DataOutputStream outputStream, final int value) 
			throws IOException {
		outputStream.write(value >> 0);
		outputStream.write(value >> 8);
		outputStream.write(value >> 16);
		outputStream.write(value >> 24);
	}
	
	private void writeShort(final DataOutputStream outputStream, final short value) 
			throws IOException {
		outputStream.write(value >> 0);
		outputStream.write(value >> 8);
	}
	
	private void writeString(final DataOutputStream outputStream, final String value) 
			throws IOException {
		for (int i = 0; i < value.length(); i++) {
			outputStream.write(value.charAt(i));
			
		}
	}
	
}
