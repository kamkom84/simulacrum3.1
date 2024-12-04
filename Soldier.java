package classesSeparated;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import static java.awt.geom.Point2D.distance;

public class Soldier extends Character {
    private final int weaponLength = 15;
    private final int maxBulletDistance = 50;
    private final int healthBarDuration = 500;
    private boolean showHealth = false;
    private int damageDealt = 0;
    private Color teamColor;
    private ScoutGame game;
    private int id;
    private int healthPoints;
    private int enemyBaseX;
    private int enemyBaseY;

    public Soldier(int x, int y, String team, int baseX, int baseY, int enemyBaseX, int enemyBaseY, ScoutGame game, int id) {
        super(x, y, team, "soldier");
        this.healthPoints = 75;////////////////////////////////////////////////////////////////////////////////////////
        this.teamColor = team.equals("blue") ? Color.BLUE : Color.RED;
        this.currentAngle = Math.toDegrees(Math.atan2(game.getHeight() / 2 - y, game.getWidth() / 2 - x));
        this.game = game;
        this.id = id;
        this.enemyBaseX = enemyBaseX;
        this.enemyBaseY = enemyBaseY;
//        this.healthPoints = 100;
    }

    public void draw(Graphics2D g2d) {
        int bodyRadius = 5;
        int lineLength = 15;

        g2d.setColor(teamColor);
        g2d.fillOval((int) (x - bodyRadius), (int) (y - bodyRadius), bodyRadius * 2, bodyRadius * 2);

        g2d.setColor(Color.YELLOW);
        int x1 = (int) x;
        int y1 = (int) y;
        int x2 = x1 + (int) (lineLength * Math.cos(Math.toRadians(currentAngle)));
        int y2 = y1 + (int) (lineLength * Math.sin(Math.toRadians(currentAngle)));
        g2d.drawLine(x1, y1, x2, y2);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 8));
        g2d.drawString("" + id, (int) x - 6, (int) y - bodyRadius - 10);

        if (showHealth) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Consolas", Font.BOLD, 10));
            g2d.drawString("" + healthPoints, (int) x - 10, (int) y - bodyRadius - 20);
        }
    }

    public void shoot(Character target) {
        if (target == null || !target.isActive()) return;

        double angleToTarget = calculateAngleTo(this.x, this.y, target.getX(), target.getY());
        double distanceToTarget = distance(this.x, this.y, target.getX(), target.getY());

        if (distanceToTarget <= weaponLength) {
            int bulletEndX = (int) (x + maxBulletDistance * Math.cos(Math.toRadians(angleToTarget)));
            int bulletEndY = (int) (y + maxBulletDistance * Math.sin(Math.toRadians(angleToTarget)));

            game.drawShot((int) x, (int) y, bulletEndX, bulletEndY);

            int damage = 2;
            target.takeDamage(damage);
            showHealthTemporarily();
            damageDealt += damage;
            System.out.println(team + " Soldier hit " + target.getType() + " for " + damage + " damage.");
        }
    }

    private double calculateAngleTo(double x1, double y1, double x2, double y2) {
        return Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
    }

    @Override
    public void takeDamage(int damage) {
        this.healthPoints -= damage;
        if (this.healthPoints <= 0) {
            this.healthPoints = 0;
            this.setActive(false);
            System.out.println("Soldier " + id + " from team " + team + " has been killed.");
        } else {
            showHealthTemporarily();
        }
    }

    private void showHealthTemporarily() {
        showHealth = true;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                showHealth = false;
            }
        }, 500);
    }

    public void moveTowardsEnemyBase() {
        double angleToBase = calculateAngleTo(this.x, this.y, enemyBaseX, enemyBaseY);
        double speed = 1.0;

        this.x += speed * Math.cos(Math.toRadians(angleToBase));
        this.y += speed * Math.sin(Math.toRadians(angleToBase));

        this.currentAngle = angleToBase;
    }

    public Character findTarget() {
        for (Character character : game.getCharacters()) {
            if (character.getTeam().equals(this.team)) continue;
            if (!character.isActive()) continue;

            double distanceToCharacter = distance(this.x, this.y, character.getX(), character.getY());
            if (distanceToCharacter <= weaponLength) {
                return character;
            }
        }
        return null;
    }

    public void update() {
        Character target = findTarget();

        if (target != null) {
            shoot(target);
        } else {
            moveTowardsEnemyBase();
        }
    }

    @Override
    public String getType() {
        return "Soldier";
    }

    public void drawPoints(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.drawString(String.valueOf(healthPoints), (int) this.x, (int) this.y - 10);
    }

    public void decreaseHealth(int amount) {
        this.healthPoints -= amount;
        if (this.healthPoints <= 0) {
            this.healthPoints = 0;
            System.out.println("Soldier " + id + " from team " + team + " has been killed.");
            this.setActive(false); // Deactivate the soldier
        } else {
            System.out.println("Soldier " + id + " from team " + team + " has " + this.healthPoints + " health left.");
            showHealthTemporarily();
            moveBack();
        }
    }

    private void moveBack() {
        double moveAngle = Math.toRadians(currentAngle + 180);
        final int MOVE_BACK_DISTANCE = 150;/////////////////////////////////////////////////////////////////////////////

        this.x += MOVE_BACK_DISTANCE * Math.cos(moveAngle);
        this.y += MOVE_BACK_DISTANCE * Math.sin(moveAngle);
    }

    public int getId() {
        return id;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public int getWeaponLength() {
        return this.weaponLength;
    }

}
