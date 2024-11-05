package classesSeparated;

import java.awt.*;
import java.util.Random;
import static java.awt.geom.Point2D.distance;

public class Worker extends Character {
    private boolean isActive = false;
    private boolean hasStarted = false;
    private boolean hasResource = false;
    private Resource targetResource = null;
    private int targetResourceIndex = -1;
    private boolean returningToBase = false;
    private boolean waitingOutsideBase = false;
    private boolean waitingInBase = false;
    private long resourceAcquisitionTime = 0;
    private long baseStayStartTime = 0;
    private Point startPosition;
    private Random random = new Random();
    private int[] resourceValues;
    private static boolean[] resourceOccupied;
    private int baseWidth;
    private int baseHeight;
    private ScoutGame scoutGame;
    private Resource[] resources;
    private int workerId;
    private static final int RESOURCE_POINTS = 5;
    private int health = 1000;
    private boolean underAttack = false;
    private long lastDamageTime = 0;
    private static final int ATTACK_DISPLAY_DURATION = 500;
    private Color color;


    public Worker(int startX, int startY, String team, Resource[] resources, int[] resourceValues,
                  boolean[] resourceOccupied, int baseWidth, int baseHeight, ScoutGame game,
                  int workerId) {
        super(startX, startY, team, "worker");
        this.scoutGame = game;
        this.resourceValues = resourceValues;
        Worker.resourceOccupied = resourceOccupied;
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.resources = resources;
        this.workerId = workerId;
        this.startPosition = new Point(startX, startY);
    }

    public void activate() {
        this.isActive = true;
        this.hasStarted = true;
        this.hasResource = false;
        this.returningToBase = false;
        this.targetResource = null;
        this.waitingOutsideBase = false;
    }

    public void updateWorkerCycle(Resource[] resources, int baseX, int baseY, Scout enemyScout) {
        if (!isActive) return;

        if (waitingOutsideBase) {
            return;
        }

        if (returningToBase) {
            returnToBase(baseX, baseY);
        } else if (hasResource) {
            gatherResource();
        } else {
            if (targetResource == null || resourceValues[targetResourceIndex] < 5) {
                if (targetResourceIndex >= 0) {
                    resourceOccupied[targetResourceIndex] = false;
                }
                targetResource = findNearestAvailableResource(resources);
                if (targetResource == null) {
                    moveOutsideBase(baseX, baseY);
                    return;
                } else {
                    resourceOccupied[targetResourceIndex] = true;
                }
            }

            if (targetResource != null) {
                moveToResource();
            }
        }

        // Update for displaying points
        update();
    }

    private Resource findNearestAvailableResource(Resource[] resources) {
        Resource nearest = null;
        double minDistance = Double.MAX_VALUE;
        int closestResourceIndex = -1;

        for (int i = 0; i < resources.length; i++) {
            if (!resourceOccupied[i] && resources[i].getValue() >= 5) {
                double distance = distance(resources[i].getX(), resources[i].getY(), this.x, this.y);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = resources[i];
                    closestResourceIndex = i;
                }
            }
        }

        if (closestResourceIndex != -1) {
            targetResourceIndex = closestResourceIndex;
            resourceOccupied[targetResourceIndex] = true;
            System.out.println("Worker " + workerId + " chose resource " + closestResourceIndex + " with " + nearest.getValue() + " points.");
        } else {
            System.out.println("Worker " + workerId + " could not find available resource.");
        }

        return nearest;
    }

    private void moveToResource() {
        if (targetResource == null) return;

        double distance = distance(targetResource.getX(), targetResource.getY(), x, y);
        double moveSpeed = 5.0;
        int resourceRadius = 20;
        int workerRadius = 6;

        if (distance > (resourceRadius + workerRadius)) {
            double moveX = ((targetResource.getX() - x) / distance) * moveSpeed;
            double moveY = ((targetResource.getY() - y) / distance) * moveSpeed;
            x += moveX;
            y += moveY;
            setAngle(Math.toDegrees(Math.atan2(targetResource.getY() - y, targetResource.getX() - x)));
        } else {
            hasResource = true;
            resourceAcquisitionTime = System.currentTimeMillis();
        }
    }

    private void gatherResource() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - resourceAcquisitionTime >= 60000) {
            if (targetResource != null && targetResource.getValue() >= RESOURCE_POINTS) {
                targetResource.reducePoints(RESOURCE_POINTS);
                hasResource = false;
                returningToBase = true;

                if (targetResource.getValue() < 5) {
                    resourceOccupied[targetResourceIndex] = false;
                }
            } else {
                hasResource = false;
                resourceOccupied[targetResourceIndex] = false;
                targetResource = null;
                targetResourceIndex = -1;
            }
        }
    }

    private void returnToBase(int baseX, int baseY) {
        int targetX = baseX + baseWidth / 2;
        int targetY = baseY + baseHeight / 2;
        double distance = distance(targetX, targetY, x, y);
        double moveSpeed = 4.0;

        if (distance > 5) {
            x += (targetX - x) / distance * moveSpeed;
            y += (targetY - y) / distance * moveSpeed;
            setAngle(Math.toDegrees(Math.atan2(targetY - y, targetX - x)));
        } else {
            long currentTime = System.currentTimeMillis();
            if (!waitingInBase) {
                waitingInBase = true;
                baseStayStartTime = System.currentTimeMillis();
            }

            if (currentTime - baseStayStartTime >= 60000) {
                addPointsToBase();
                prepareForNextCycle();
            }
        }
    }

    private void moveOutsideBase(int baseX, int baseY) {
        int waitX = startPosition.x;
        int waitY = startPosition.y;
        double distance = distance(waitX, waitY, x, y);
        double moveSpeed = 4.0;

        if (distance > 5) {
            x += (waitX - x) / distance * moveSpeed;
            y += (waitY - y) / distance * moveSpeed;
            setAngle(Math.toDegrees(Math.atan2(waitY - y, waitX - x)));
        } else {
            waitingOutsideBase = true;
        }
    }

    private void prepareForNextCycle() {
        returningToBase = false;
        waitingInBase = false;
        hasResource = false;
        targetResource = null;
        waitingOutsideBase = false;
        if (targetResourceIndex >= 0) {
            resourceOccupied[targetResourceIndex] = false;
        }
        targetResourceIndex = -1;
    }

    private void addPointsToBase() {
        scoutGame.addPointsToScoutBase(team, RESOURCE_POINTS);
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            setInactive();
        } else {
            underAttack = true;
            lastDamageTime = System.currentTimeMillis();
        }
    }

    public boolean shouldDisplayPoints() {
        long currentTime = System.currentTimeMillis();
        return underAttack && (currentTime - lastDamageTime < ATTACK_DISPLAY_DURATION);
    }

    private void update() {
        if (underAttack && System.currentTimeMillis() - lastDamageTime > 2000) {
            underAttack = false;
        }
    }

    public void draw(Graphics2D g2d) {
        int bodyRadius = 5;
        g2d.setColor(team.equals("blue") ? new Color(0, 100, 255) : new Color(200, 50, 50));
        g2d.fillOval((int) (x - bodyRadius), (int) (y - bodyRadius), bodyRadius * 2, bodyRadius * 2);

        if (shouldDisplayPoints()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Health: " + health, (int) x - 10, (int) y - 10);
        }
    }

    public void setInactive() {
        this.isActive = false;
        this.hasStarted = false;
        this.targetResource = null;
        this.targetResourceIndex = -1;
        this.returningToBase = false;
        this.hasResource = false;
        this.waitingOutsideBase = false;
        this.waitingInBase = false;
        this.x = -1000;  // Moves the worker off-screen
        this.y = -1000;
        System.out.println("Worker " + workerId + " of team " + team + " is now inactive.");
    }

    public String getTeam() {
        return this.team;
    }

    public boolean isWorkingOn(Resource resource) {
        return this.targetResource != null && this.targetResource.equals(resource);
    }

    public void setAngle(double angle) {
        this.currentAngle = angle;
    }

    public boolean isUnderAttack() {
        long currentTime = System.currentTimeMillis();
        return underAttack && (currentTime - lastDamageTime <= ATTACK_DISPLAY_DURATION);
    }

    public int getHealth() {
        return health;
    }

    public int getBodyRadius() {
        return 5; // Example radius value for the worker
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
