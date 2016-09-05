
package vlcjtest;

/*
 * Example downloaded from the 
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class RewardMediaPlayer {
	
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

    private RewardMediaPlayer(String[] args) {
        JFrame frame = new JFrame("The Reward Mediaplayer");

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        final EmbeddedMediaPlayer video = mediaPlayerComponent.getMediaPlayer();
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(mediaPlayerComponent, BorderLayout.CENTER);
        
        frame.setContentPane(panel);

        JPanel subPanel = new JPanel(new GridLayout(5,1));       
        
        JButton btnMute = new JButton("Mute");
        //panel.add(btnMute, BorderLayout.NORTH);
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
        
        JButton btnStop = new JButton("Stop");
        //panel.add(btnMute, BorderLayout.NORTH);
        subPanel.add(btnStop);
        btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.stop();
			}
		});
        
        JButton btnReplay = new JButton("Replay");
        //panel.add(btnMute, BorderLayout.NORTH);
        subPanel.add(btnReplay);
        btnReplay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        video.playMedia("big_buck_bunny_1_minute.avi");
				video.mute(false);
			}
		});
         
        panel.add(subPanel, BorderLayout.EAST);
        
        JLabel labelTime = new JLabel("0 seconds");
        panel.add(labelTime, BorderLayout.SOUTH);

        Timer timer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long time = (long)(video.getTime()/1000.0);
				labelTime.setText(String.valueOf(time)+" seconds");
			}
		});
        timer.start();
        
        frame.setLocation(100, 100);
        frame.setSize(1050, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        String filename = "big_buck_bunny_1_minute.avi";
        video.playMedia(filename);
        video.mute(false);
    }

    public static void main(final String[] args) {
        
        NativeLibrary.addSearchPath(
            RuntimeUtil.getLibVlcLibraryName(), "/Applications/vlc-2.0.0/VLC.app/Contents/MacOS/lib"
        );
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RewardMediaPlayer(args);
            }
        });
    }
}