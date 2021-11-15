package brains;

import java.util.List;

import edu.unlam.snake.brain.Brain;
import edu.unlam.snake.engine.Direction;
import edu.unlam.snake.engine.Point;

public class ElGarrote extends Brain {
	private int escapingDistance;
	private int maxLength;
	private int rechargeTime;
	private int time;
	private int scanner;
	private int lastSize;
	
	public ElGarrote() {
		super("El Dia Del Garrote");
		escapingDistance = 8;
		maxLength = 15;
		rechargeTime = 20;
		time = 0;
		scanner = 0;
		lastSize = 0;
	}
	
	/**
	 * Retorna el proximo movimiento que debe hacer la serpiente.
	 * 
	 * @param head
	 *			, la posicion de la cabeza
	 * @param previous
	 *			, la direccion en la que venia moviendose.
	 */
	public Direction getDirection(Point head, Direction previous) {
		List<Point> snake = info.getSnake();
		Point closestFruit = getClosestFruit(head);
		Point closestEnemie = getClosestSnake(head);
		Direction wantedDirection = previous;
		
		if(getDistanceBetween(head, closestEnemie) < escapingDistance) {
			wantedDirection = getDirectionAwayFrom(closestEnemie, wantedDirection, previous, head);
		} else {
			if(snake.size() < maxLength) {
				wantedDirection = getDirectionTo(closestFruit, wantedDirection, previous, head);
			} else {
				if(time < rechargeTime){
					time++;
				} else {
					time = 0;
					scanner++;
				}
				
				if(getDistanceBetween(head, closestFruit) < scanner){
					wantedDirection = getDirectionTo(closestFruit, wantedDirection, previous, head);
				}
				
				if(lastSize == snake.size()-1){
					scanner = 0;
				}
			}
		}
		
		if(getDistanceBetween(closestEnemie, closestFruit) < getDistanceBetween(head, closestFruit) &&
			getDistanceBetween(closestEnemie, closestFruit) < 5){
			wantedDirection = getDirectionAwayFrom(closestFruit, wantedDirection, previous, head);
		}
		
		// EVITAR OBSTACULOS
		wantedDirection = getDirectionAvoidDanger(wantedDirection, previous, head);
		
		lastSize = snake.size();
		
		return wantedDirection;
	}
	
	/**
	 * @return
	 *			devuelve la direccion necesaria para alejarse de un punto
	 */
	private Direction getDirectionAwayFrom(Point objective, Direction wantedDirection,Direction previous,Point head) {
		Point turnedLeft = previous.turnLeft().move(head);
		Point turnedRight = previous.turnRight().move(head);
		Point movedPrevious = previous.move(head);
		
		int prevObj = getDistanceBetween(movedPrevious, objective);
		int leftObj = getDistanceBetween(turnedLeft, objective);
		int rightObj = getDistanceBetween(turnedRight, objective);
		
		if(prevObj >= leftObj && prevObj >= rightObj){
			wantedDirection = previous;
		} else if(leftObj > rightObj){
			wantedDirection = previous.turnLeft();
		} else if(rightObj > leftObj){
			wantedDirection = previous.turnRight();
		} else {
			if(getDirectionAvoidDanger(previous.turnLeft(), previous, head) == previous.turnLeft()){
				wantedDirection = previous.turnLeft();
			} else {
				wantedDirection = previous.turnRight();
			}
		}
		
		return wantedDirection;
	}
	
	/**
	 * @return
	 * 			devuelve la direccion necesaria para acercarse a un punto
	 */
	private Direction getDirectionTo(Point objective, Direction wantedDirection,Direction previous,Point head){
		Point turnedLeft = previous.turnLeft().move(head);
		Point turnedRight = previous.turnRight().move(head);
		Point movedPrevious = previous.move(head);
		
		int prevObj = getDistanceBetween(movedPrevious, objective);
		int leftObj = getDistanceBetween(turnedLeft, objective);
		int rightObj = getDistanceBetween(turnedRight, objective);
		
		if(prevObj <= leftObj && prevObj <= rightObj){
			wantedDirection = previous;
		} else if(leftObj < rightObj){
			wantedDirection = previous.turnLeft();
		} else if(rightObj < leftObj){
			wantedDirection = previous.turnRight();
		} else {
			if(getDirectionAvoidDanger(previous.turnLeft(), previous, head) == previous.turnLeft()){
				wantedDirection = previous.turnLeft();
			} else {
				wantedDirection = previous.turnRight();
			}
		}
		
		return wantedDirection;
	}
	
	/**
	 * @return un array de 3 booleanos que representan si la osision a la izquierda de head,
	 * 		   al frente de head o a la derecha de head esta ocupada con algun obstaculo
	 * 		   (el propio snake, un enemigo o una pared), en ese orden.	
	 */
	private boolean[] getTakenPositions(Direction previous, Point head) {
		List<Point> snake = info.getSnake();
		List<List<Point>> enemies = info.getEnemies();
		List<Point> obstacles = info.getObstacles();
		
		boolean isFrontTaken = false;
		boolean isLeftTaken = false;
		boolean isRightTaken = false;
		
		Point frontPos = previous.move(head);
		Point leftPos = previous.turnLeft().move(head);
		Point rightPos = previous.turnRight().move(head);
		
		// Esta mi cuerpo en alguna de las posiciones?
		for(Point myBody : snake) {
			if(frontPos.equals(myBody))
				isFrontTaken = true;
			
			if(leftPos.equals(myBody))
				isLeftTaken = true;
			
			if(rightPos.equals(myBody))
				isRightTaken = true;
		}
		
		// Hay una pared en alguna de las posiciones?
		for(Point obstacle : obstacles) {
			if(frontPos.equals(obstacle))
				isFrontTaken = true;
			
			if(leftPos.equals(obstacle))
				isLeftTaken = true;
			
			if(rightPos.equals(obstacle))
				isRightTaken = true;
		}
		
		// Hay algun enemigo en alguna de las posiciones?
		for(List<Point> enemie : enemies) {
			for(Point body : enemie) {
				if(frontPos.equals(body))
					isFrontTaken = true;
				
				if(leftPos.equals(body))
					isLeftTaken = true;
				
				if(rightPos.equals(body))
					isRightTaken = true;
			}
		}
		
		boolean[] positions = { isLeftTaken, isFrontTaken, isRightTaken };
		
		return positions;
	}
	
	/**
	 * @return la direccion a la que tengo que ir para evitar los obstaculos evitando entrar
	 *		   en una posicion que no tenga salida inmediata
	 */
	private Direction getDirectionAvoidDanger(Direction wanted, Direction previous, Point head) {
		boolean[] positions = getTakenPositions(previous, head);
		
		boolean isLeftTaken = positions[0];
		boolean isFrontTaken = positions[1];
		boolean isRightTaken = positions[2];
		
		// Si la posicion no tiene salida inmediata entonces la considero como ocupada
		if(!isLeftTaken) {
			boolean[] goLeft = getTakenPositions(previous.turnLeft(), previous.turnLeft().move(head));
			
			if(goLeft[0] && goLeft[1] && goLeft[2])
				isLeftTaken = true;
		}
		
		if(!isFrontTaken) {
			boolean[] goFront = getTakenPositions(previous, previous.move(head));
			
			if(goFront[0] && goFront[1] && goFront[2])
				isFrontTaken = true;
		}
		
		if(!isRightTaken) {
			boolean[] goRight = getTakenPositions(previous.turnRight(), previous.turnRight().move(head));
			
			if(goRight[0] && goRight[1] && goRight[2])
				isRightTaken = true;
		}
		
		// La direccion que se quiere obtener depende de la disponibilidad de la posicion
		if(wanted == previous.turnLeft()) {
			if(isLeftTaken) {
				if(isFrontTaken)
					wanted = previous.turnRight();
				else
					wanted = previous;
			}
		} else if(wanted == previous.turnRight()) {
			if(isRightTaken) {
				if(isFrontTaken)
					wanted = previous.turnLeft();
				else
					wanted = previous;
			}
		} else if(wanted == previous) {
			if(isFrontTaken) {
				if(isRightTaken)
					wanted = previous.turnLeft();
				else
					wanted = previous.turnRight();
			}
		}
		
		return wanted;
	}
	
	/**
	 * @return el punto en donde esta la cabeza del enemigo mas cercano desde un punto especifico
	 */
	private Point getClosestSnake(Point from) {
		List<List<Point>> enemies = info.getEnemies();
		Point closestSnake = new Point(1000, 1000);
		
		for(List<Point> enemie : enemies) {
			if(getDistanceBetween(from, enemie.get(0)) < getDistanceBetween(from, closestSnake))
				closestSnake = enemie.get(0);
		}
		
		return closestSnake;
	}
	
	/**
	 * @return el punto en donde esta la fruta mas cercana desde un punto especifico
	 */
	private Point getClosestFruit(Point from) {
		List<Point> fruits = info.getFruits();
		Point closestFruit = new Point(1000, 1000);
		
		for(Point fruit : fruits) {
			if(getDistanceBetween(from, fruit) < getDistanceBetween(from, closestFruit))
				closestFruit = fruit;
		}
		
		return closestFruit;
	}
	
	/**
	 * @return la distancia Manhattan entre dos puntos
	 */
	private int getDistanceBetween(Point point1, Point point2) {
		return (Math.abs(point1.getX() - point2.getX()) + Math.abs(point1.getY() - point2.getY()));
	}
}