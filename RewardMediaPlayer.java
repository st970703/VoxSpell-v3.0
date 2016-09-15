
/*
 * Example downloaded from the 
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class RewardMediaPlayer{
	
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private String _filename;

    public RewardMediaPlayer(JPanel panel, final SpellingAid parent, String filename) {
//        JFrame frame = new JFrame("The Reward Mediaplayer");
//        frame.setAlwaysOnTop(true);

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        _filename = filename;
        
        final EmbeddedMediaPlayer video = mediaPlayerComponent.getMediaPlayer();
        
//        JPanel panel = new JPanel(new BorderLayout());
        panel.add(mediaPlayerComponent, BorderLayout.CENTER);
        
//        frame.setContentPane(panel);
        
        video.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
        	@Override
        	public void finished(MediaPlayer mediaPlayer) {
        		video.stop();
        		parent.switchScreens("MAIN");
        		parent.levelCompleted();
        	}
        });

        JPanel subPanel = new JPanel(new GridLayout(6,1));       
        
        JButton btnMute = new JButton("Mute");
//        panel.add(btnMute, BorderLayout.NORTH);
        subPanel.add(btnMute);
        btnMute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.mute();
			}
		});
        
        JButton btnSkip = new JButton("Forward 5 seconds");
        btnSkip.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.skip(5000);
			}
		});
        //panel.add(btnSkip, BorderLayout.EAST);
        subPanel.add(btnSkip);
        
        JButton btnSkipBack = new JButton("Backward 5 seconds");
        btnSkipBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.skip(-5000);
			}
		});
        //panel.add(btnSkipBack, BorderLayout.WEST);
        subPanel.add(btnSkipBack);
        
        JButton btnPause = new JButton("Pause/Resume");
        //panel.add(btnMute, BorderLayout.NORTH);
        subPanel.add(btnPause);
        btnPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.pause();
			}
		});
        
        JButton btnStop = new JButton("Stop");
        //panel.add(btnMute, BorderLayout.NORTH);
        subPanel.add(btnStop);
        btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.stop();
				parent.switchScreens("MAIN");
				parent.levelCompleted();
			}
		});
        
        JButton btnReplay = new JButton("Replay");
        //panel.add(btnMute, BorderLayout.NORTH);
        subPanel.add(btnReplay);
        btnReplay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        video.playMedia(_filename);
				video.mute(false);
			}
		});
        
         
        panel.add(subPanel, BorderLayout.EAST);
        
        final JLabel labelTime = new JLabel("0 seconds");
        panel.add(labelTime, BorderLayout.SOUTH);

        Timer timer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long time = (long)(video.getTime()/1000.0);
				long totalTime = (long)(video.getLength()/1000.0);
				//System.out.println(totalTime);
				labelTime.setText(String.valueOf(time)+ " / " + String.valueOf(totalTime)+" seconds");
			}
		});
        timer.start();
        
        panel.setLocation(100, 100);
        panel.setSize(1050, 600);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.setVisible(true);

        //String filename = "big_buck_bunny_1_minute_aecho.avi";
        //String filename = "big_buck_bunny_1_minute.avi";
        video.playMedia(_filename);
        video.mute(false);
    }

    /*public static void main(final String[] args) {
        
        NativeLibrary.addSearchPath(
            RuntimeUtil.getLibVlcLibraryName(), "/Applications/vlc-2.0.0/VLC.app/Contents/MacOS/lib"
        );
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        
        JFrame frame = new JFrame();
        final JPanel panel = new JPanel();
        frame.add(panel);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RewardMediaPlayer(panel, null);
            }
        });
    }*/
}