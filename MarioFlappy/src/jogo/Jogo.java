/*
 * Desenvolvido Por Fernando S. Nascimento e Mauri Victor Zellner. 
 * Date: 01/08/2015 
 * 
 * */

package jogo;


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;





public class Jogo extends MIDlet implements CommandListener{
	
	private Display display;
	private JogoCanvas telaPrincipal;
	private Command sair;
	private Command pause;
	private Command start;

	public Jogo() {
		sair  = new Command("Sair", Command.EXIT, 0);
		start = new Command("Start", Command.ITEM, 0);
		pause = new Command("Pause", Command.ITEM, 0);
		
		
		telaPrincipal = new JogoCanvas();
		telaPrincipal.addCommand(sair);
		telaPrincipal.addCommand(start);		
		telaPrincipal.setCommandListener(this);	
		
	}
	
	
	public void commandAction(Command comando, Displayable display) {
		if (comando == sair) {
			notifyDestroyed();
			return;
		}
		
		if (comando == pause) {
			pauseApp();
			return;
		}	
		if (comando == start) {
			try {				
				telaPrincipal.RestartJogo();				
				startApp();
			} catch (MIDletStateChangeException e) {
				e.printStackTrace();
			}
			return;
		}	
	}
	
	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		display.setCurrent(telaPrincipal);		
		return;
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	

	}

	protected void pauseApp() {
		
		return;

	}


}
