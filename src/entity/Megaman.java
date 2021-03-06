package entity;


import java.util.ArrayList;
import javax.imageio.ImageIO;


import java.awt.*;
import java.awt.image.BufferedImage;

public class Megaman extends Entity {
	
	
	private ArrayList<Bullet> bullets;
	
	// animation actions
	private static final int IDLE = 0; 
	private static final int WALKING = 1;
	private static final int JUMPING = 2;
	private static final int FALLING = 3;
	private static final int SHOOTING = 4;
	private static final int PUNCHING = 5;
	private static final int SLIDING = 6;
	
	// animations
	private ArrayList<BufferedImage[]> sprites;
	private final int[] numFrames = {
		1, 3, 1, 1, 3, 2, 1
	};
	
	
	
	public Megaman(double floor) {
		
		super(floor);
		
		width = 30;
		height = 30;
		collisionWidth = 12;
		collisionHeight = 12;
		
		// x
		moveSpeed = 0.3;
		maxSpeed = 2.0;
		stopSpeed = 0.5;
		slideSpeed = 4.5;
		// y
		fallSpeed = 0.5;
		maxFallSpeed = 4.0;
		jumpStart = -7.8;
		stopJumpSpeed = 0.3;
		
		isFacingRight = true;
		
		health = maxHealth = 80;
		
		shootDamage = 2;
		bullets = new ArrayList<Bullet>();
		punchDamage = 3;
		punchRange = 40;
		
		// load sprites
		try {
			
			BufferedImage spriteSheet = ImageIO.read(getClass().getClassLoader().getResourceAsStream("resources/sprites/player/megamanSpriteSheet.png"));
			
			sprites = new ArrayList<BufferedImage[]>();
			for (int i = 0; i < 7; i++) {
				BufferedImage[] bi = new BufferedImage[numFrames[i]];
				
				for(int j = 0; j < numFrames[i]; j++) {
					
					bi[j] = spriteSheet.getSubimage(j * width, i * height, width, height);
					
				}
				
				sprites.add(bi);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		animation = new Animation();
		currentAction = IDLE;
		animation.setFrames(sprites.get(IDLE));
		animation.setDelay(400);
		isFalling = true;
	}
	
	public void checkProjectiles(Entity enemy) {
		// shots
		for (int i = 0; i < bullets.size(); i++) {
			if(bullets.get(i).intersects(enemy)) {
				enemy.wasHit(shootDamage);
				bullets.get(i).setHit();
			}
		}
	}
	
	public void checkAttack(Entity enemy) {
		
		this.checkProjectiles(enemy);
		super.checkCloseAttack(enemy);
	}
	
	// this function determines where the next position of the player is by reading keyboard input
	private void getNextPosition() {
		
		if(isSliding) {
			if(this.getFacingRight()) {
				dx += slideSpeed;
				if(dx > slideSpeed) dx = slideSpeed;
			}
			else {
				dx -= slideSpeed;
				if(dx < -slideSpeed) dx = -slideSpeed;
			}
		}
		
		// movement
		if(isLeft) {
			if(isSliding) {
				dx -= slideSpeed;
				if(dx < -slideSpeed) {
					dx = -slideSpeed;
				}
			}
			else {
				dx -= moveSpeed;
				if(dx < -maxSpeed) {
					dx = -maxSpeed;
				}
			}
		}
		else if(isRight) {
			if(isSliding) {
				dx += slideSpeed;
				if(dx > slideSpeed) {
					dx = slideSpeed;
				}
			}
			else {
				dx += moveSpeed;
				if(dx > maxSpeed) {
					dx = maxSpeed;
				}
			}
		}
		else {
			if(dx > 0) {
				dx -= stopSpeed;
				if(dx < 0) {
					dx = 0;
				}
			}
			else if(dx < 0) {
				dx += stopSpeed;
				if(dx > 0) {
					dx = 0;
				}
			}
		}
		
		// cannot move while attacking unless in the air yet
		if((currentAction == PUNCHING || currentAction == SHOOTING) && !(isJumping || isFalling)) {
			// cannot move;
			dx = 0;
		}
		
		// jumping
		if(isJumping && !isFalling) {
			dy = jumpStart;
			isFalling = true;
		}
		
		// falling
		if(isFalling) {
			
			dy += fallSpeed;
			
			if(dy > 0) isJumping = false;
			 
		}
		
	}
	
	public void update() {
		
		// update position
		
		getNextPosition();
		checkTileMapCollision();
		setPosition(xtemp, ytemp);
		
		// check attacks
		if(currentAction == PUNCHING) {
			if(animation.hasPlayedOnce()) isPunching = false;
		}
		if(currentAction == SHOOTING) {
			if(animation.hasPlayedOnce()) isShooting = false;
		}
		
		// shooting attack
		if(isShooting && currentAction != SHOOTING) {
			Bullet b = new Bullet(floor, isFacingRight);
			b.setPosition(x, y);
			bullets.add(b);
		}
		// update bullets
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).update();
			if(bullets.get(i).shouldRemove()) {
				bullets.remove(i);
				i--;
			}
		}
		
		// check sliding
		if(currentAction == SLIDING) {
			if(animation.hasPlayedOnce()) isSliding = false;
		}
		
		// fix bug of spamming shots
		if(isPunching && isShooting) {
			isShooting = false;
		}
		// set animation
		if(isPunching) {
			if(currentAction != PUNCHING) {
				currentAction = PUNCHING;
				animation.setFrames(sprites.get(PUNCHING));
				animation.setDelay(100);
				width = 30;
			}
		}
		else if(isShooting) {
			if(currentAction != SHOOTING) {
				currentAction = SHOOTING;
				animation.setFrames(sprites.get(SHOOTING));
				animation.setDelay(70);
				width = 30;
			}
		}
		else if(isSliding) {
			if(currentAction != SLIDING) {	
				currentAction = SLIDING;
				animation.setFrames(sprites.get(SLIDING));
				animation.setDelay(300);
				width = 30;
			}
		}
		else if(isFalling) {
			if(currentAction != FALLING) {
				currentAction = FALLING;
				animation.setFrames(sprites.get(FALLING));
				animation.setDelay(-1);
				width = 30;
			}
		
		}
		else if(isJumping) {
			if(currentAction != JUMPING) {
				currentAction = JUMPING;
				animation.setFrames(sprites.get(JUMPING));
				animation.setDelay(-1);
				width = 30;
			}
		}
		
		else if(isLeft || isRight) {
			if(currentAction != WALKING) {
				currentAction = WALKING;
				animation.setFrames(sprites.get(WALKING));
				animation.setDelay(100);
				width = 30;
			}
		}
		else {
			if(currentAction != IDLE) {
				currentAction = IDLE;
				animation.setFrames(sprites.get(IDLE));
				animation.setDelay(400);
				width = 30;
			}
		}
		
		animation.update();
		
		// set direction
		// the player currently cannot move while attacking 
		
		 
		if(isRight) isFacingRight = true;
		if(isLeft) isFacingRight = false;
		
		if(currentAction != PUNCHING && currentAction != PUNCHING) {
			
		}
		super.update();
	}
	
	
	public void draw(Graphics2D graphics) {
		
		
		// draw bullets
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).draw(graphics);
		}
		
		// flinching mechanic that blinks the player when he takes damage
		if(isFlinching) {
			long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
			if(elapsed / 100 % 2 == 0) {
				return;
			}
		}
		
		super.draw(graphics);
	}
	
	
}
