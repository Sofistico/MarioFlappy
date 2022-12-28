package jogo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;


public class JogoCanvas extends GameCanvas {
	private Graphics tela;
	private LayerManager lm;
	private Sprite personagem;
	private Sprite inimigoBomba;
	private Sprite inimigo;
	private Sprite casco;
	private Sprite moeda;
	private Sprite moedaCont;
	private Sprite scenario;
	private Sprite fimDoJogo;
	private Sprite fundo;
	private Sprite vida;
	
	private boolean gameOver;

	private InputStream somMoeda;
	private InputStream lostlife;
	private InputStream capeRise;
	private InputStream stomp;
	private InputStream sGameOver;

	private Player player1;
	private Player player2;
	private Player player3;
	private Player player4;
	private Player player5;

	
	private int personagemCima[]  = new int[] { 0, 1, 2, 3 };
	private int personagemBaixo[] = new int[] { 4, 5, 6, 7 };
	private int personagemChao[]  = new int[] { 8, 9, 10, 11 };
	private int personagemExplosao[] = new int[] { 0, 1, 2, 3, 4 };
	private int morrendo[] = new int[] { 0, 1 };

	private int speedMove = 10;
	private int contMoedas = 0;
	private int contMoedastemp = 0;
	private int tempoSleep = 100;
	private int largura;
	private int altura;
	private int vidas = 3;
	
	Image imgPersonagem;
	Image imgMoeda;
	Image imgFogo;
	Image imgMarioMorre;
	Image imgVida;
	Image imgFundo;
	
	Image imgInimigo;
	Image imgBomba;
	Image imgCasco;
	Image imgGameOver;
	Image imgBackground;

	protected JogoCanvas(boolean suppressKeyEvents) {
		super(suppressKeyEvents);

	}
	
	
	public JogoCanvas() {
		this(false);
		tela = getGraphics();
		lm = new LayerManager();
		gameOver = false;
		
		Alert tmp = new Alert("Buscando tamanho da Tela.");
		largura = tmp.getWidth();
		altura = tmp.getHeight();	

		try {
			
			imgGameOver = Image.createImage("/gameOver.png");
			imgFundo = Image.createImage("/fundo.png");
			imgPersonagem = Image.createImage("/pvoa.png");
			imgBackground = Image.createImage("/cenario3.jpg");
			imgInimigo = Image.createImage("/inimigo.png");
			imgBomba = Image.createImage("/bomba.png");
			imgCasco = Image.createImage("/casco.png");
			imgMoeda = Image.createImage("/moeda.png");
			imgFogo = Image.createImage("/fogo.png");
			imgMarioMorre = Image.createImage("/mariomorre.png");
			imgVida= Image.createImage("/painel.png");
			

			somMoeda = getClass().getResourceAsStream("/coin.wav");
			stomp    = getClass().getResourceAsStream("/smw_stomp.wav");
			lostlife = getClass().getResourceAsStream("/smw_lost_a_life.wav");
			sGameOver = getClass().getResourceAsStream("/smw_game_over.wav");
			capeRise = getClass().getResourceAsStream("/smw_cape_rise.wav");

			scenario = new Sprite(imgBackground);
			fundo = new Sprite(imgFundo);
			fimDoJogo = new Sprite(imgGameOver);
			
			vida = new Sprite(imgVida);
			vida.move(getWidth()-108, 3);
			
			moedaCont = new Sprite(imgMoeda, 14, 21);
			moedaCont.move(getWidth()-50, 0);
			
			moeda = new Sprite(imgMoeda, 14, 21);
			moeda.move(700, 30);

			inimigo = new Sprite(imgInimigo, 36, 32);
			inimigo.move(400, 40);

			casco = new Sprite(imgCasco, 19, 19);
			casco.move(490, 80);

			inimigoBomba = new Sprite(imgBomba, 54, 32);
			inimigoBomba.move(300, 190);

			personagem = new Sprite(imgPersonagem, 28, 32);
			personagem.move(40, 150);
			personagem.defineReferencePixel(14, 16);

			lm.append(moedaCont);
			lm.append(vida);
						
			lm.append(moeda);
			lm.append(personagem);
			lm.append(inimigo);
			lm.append(inimigoBomba);
			lm.append(casco);
			lm.append(fimDoJogo);	
			lm.append(fundo);
			lm.append(scenario);
			lm.paint(tela, 0, 0);

			player1 = Manager.createPlayer(somMoeda, "audio/x-wav");
			player2 = Manager.createPlayer(lostlife, "audio/x-wav");
			player3 = Manager.createPlayer(capeRise, "audio/x-wav");
			player4 = Manager.createPlayer(sGameOver, "audio/x-wav");
			player5 = Manager.createPlayer(stomp, "audio/x-wav");


			flushGraphics();
			tratadorDeEventos();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (MediaException e) {

			e.printStackTrace();
		}

	}

	private void tratadorDeEventos() {
		new Thread() {
			public void run() {
				int lastPressedKey = 0;
				while (!gameOver) {
					try {
						fimDoJogo.setVisible(false);
						fundo.setVisible(false);
						int keyState = getKeyStates();
						// Movimentos das Sprites
						movimentoSprites(moeda);					
						movimentoSprites(casco);
						movimentoSprites(inimigo);
						movimentoSprites(inimigoBomba);
						
						if (keyState != lastPressedKey) {
							switch (keyState) {
							case UP_PRESSED:
								personagem.setFrameSequence(personagemCima);
								
								break;
							case DOWN_PRESSED:
								personagem.setFrameSequence(personagemBaixo);
									
								break;
							default:
								personagem.setFrameSequence(personagemBaixo);	
								break;
								
							}
							repaint();
						}
						switch (keyState) {
						case DOWN_PRESSED:
							
							if (personagem.getY() + personagem.getHeight() < getHeight() - 20) {
								personagem.move(0, speedMove);
							}
							// Tratamento de colisão pega moeda
							colisaoMoedas();
							
							// Tratamento de colisão atingir inimigo
							if ((personagem.collidesWith(inimigoBomba, true))
									|| (personagem.collidesWith(inimigo, true))
									|| (personagem.collidesWith(casco, true))) {
								
								player5.start();
								
								if(personagem.collidesWith(inimigoBomba, true)){
									inimigoBomba.move(400, 200);
								}
								if(personagem.collidesWith(inimigo, true)){
									inimigo.move(400, 200);
								}
								if(personagem.collidesWith(casco, true)){
									casco.move(400, 200);
								}
							}
							
							
							personagem.nextFrame();
							lastPressedKey = DOWN_PRESSED;
							break;
							
						case UP_PRESSED:
							if (personagem.getY() > 0) {
								personagem.move(0, -speedMove);
								player3.start();
							}
							// Tratamento de colisão pega moeda
							colisaoMoedas();
							
							// Tratamento de colisão morrendo
							if ((personagem.collidesWith(inimigoBomba, true))
									|| (personagem.collidesWith(inimigo, true))
									|| (personagem.collidesWith(casco, true))) {								
								
									gameOver();	
							}
							
							personagem.nextFrame();
							lastPressedKey = UP_PRESSED;
							break;
							
						default:
							if (personagem.getY() + personagem.getHeight() < getHeight() - 20) {
								personagem.move(0, 3);
							}
							// Tratamento de colisão pega moeda
							colisaoMoedas();
							
							// Tratamento de colisão morrendo
							if ((personagem.collidesWith(inimigoBomba, true))
									|| (personagem.collidesWith(inimigo, true))
									|| (personagem.collidesWith(casco, true))) {								
								
									gameOver();									
							}						
							personagem.nextFrame();	
							lastPressedKey = DOWN_PRESSED;
							break;
						}						
						

						lm.paint(tela, 0, 0);
						
						tela.setColor(99999999);	
						tela.drawString(" = ",  getWidth()-75, 3, Graphics.TOP | Graphics.RIGHT);
						tela.drawString(Integer.toString(vidas), getWidth()-60, 3, Graphics.TOP | Graphics.RIGHT); 
						tela.drawString(" = ",  getWidth()-25, 3, Graphics.TOP | Graphics.RIGHT);
						tela.drawString(Integer.toString(contMoedas), getWidth()-10, 3, Graphics.TOP | Graphics.RIGHT); 
						
						flushGraphics();
						
						// a cada 5 moedas capturadas aumenta a velocidade do jogo
						if(contMoedastemp == 5){
							speedMove += 5;
							contMoedastemp = 0;
						}
						
						Thread.sleep(tempoSleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (MediaException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	private void gameOver() throws MediaException, InterruptedException {
		
		if (personagem.collidesWith(inimigoBomba, true)) {
			inimigoBomba.move(400, 200);
			personagem.setImage(imgFogo, 71, 70);
			personagem.setFrameSequence(personagemExplosao);
			for(int i = 0; i <= 40000; i++){
				personagem.nextFrame();
			}		}
		if (personagem.collidesWith(inimigo, true)) {
			inimigo.move(400, 200);
			personagem.setImage(imgMarioMorre, 21, 35);
			personagem.setFrameSequence(morrendo);
			for(int i = 0; i <= 10000; i++){
				personagem.nextFrame();
			}
		}
		if (personagem.collidesWith(casco, true)) {
			casco.move(400, 200);
			personagem.setImage(imgMarioMorre, 21, 35);
				personagem.setFrameSequence(morrendo);
				for(int i = 0; i <= 10000; i++){
					personagem.nextFrame();
				}
		}
		vidas--;
		encerrarJogo();

	}
	
	private void colisaoMoedas() throws MediaException {
		
		if (personagem.collidesWith(moeda, true)) {
			player1.start();
			contMoedas++;
			contMoedastemp++;
			moeda.move(400, 100);

		}
	}
	
	private void movimentoSprites(Sprite sprite){
		Random r = new Random();
		int posicaoX = 0;
		
		if(sprite.equals(inimigoBomba)){
			posicaoX = 300;
		}else if(sprite.equals(inimigo)){
			posicaoX = 400;
		}else if(sprite.equals(casco)){
			posicaoX = 500;
		}else if(sprite.equals(moeda)){
			posicaoX = 600;
		}
		
		sprite.nextFrame();

		if (sprite.getX() > 10) {
			sprite.move(-speedMove, 0);
		} else {
			if (sprite.getY() < getHeight() - 30) {
				sprite.move(posicaoX, r.nextInt(sprite.getY()));
			} else {
				sprite.move(posicaoX + 50, -(r.nextInt(sprite.getY())));
			}
		}
		
	}
	
    private void encerrarJogo() throws MediaException{
		
		gameOver = true;
		casco.setVisible(false);
		inimigoBomba.setVisible(false);
		inimigo.setVisible(false);
		moeda.setVisible(false);
		
		if(vidas == 0){
			
			player4.start();
			vidas = 3;
			contMoedas = 0;
			speedMove = 10;
			personagem.setVisible(false);			
			fimDoJogo.move(((largura / 2) - (fimDoJogo.getWidth() / 2)), ((altura / 2 ) - (fimDoJogo.getHeight() / 2))); 
			fimDoJogo.setVisible(true);
			fundo.setVisible(true);
			
		}else{
			player2.start();
		}
	}
	
	public void RestartJogo(){
		
		gameOver = false;		
		personagem.setImage(imgPersonagem, 28, 32);		
		casco.setVisible(true);
		inimigoBomba.setVisible(true);
		inimigo.setVisible(true);
		personagem.setVisible(true);
		moeda.setVisible(true);		
		tempoSleep = 100;		
		contMoedastemp = 0;
		tratadorDeEventos();
	}
	
}

