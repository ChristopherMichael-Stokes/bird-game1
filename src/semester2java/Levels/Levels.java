/*
 * The Levels object is a collection of objects type Level.  This is 
 * where the levels are managed, and where the certain body objects  (such
 * as Player) are passed through to the levels
 *
 */
package semester2java.Levels;

import semester2java.Levels.levels.Level2;
import semester2java.Levels.levels.Level1;
import city.cs.engine.StepEvent;
import city.cs.engine.StepListener;
import city.cs.engine.UserView;
import city.cs.engine.World;
import java.util.EnumSet;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLayeredPane;
import org.jbox2d.common.Vec2;
import semester2java.Bodies.Player;
import semester2java.Controller.KeyboardHandler;
import semester2java.Controller.MouseHandler;
import semester2java.Levels.Event.ChangeLevelListener;
import semester2java.Levels.Event.EndGameListener;
import semester2java.Semester2Java;

/**
 *
 * @author Christopher
 */
public class Levels implements ChangeLevelListener, StepListener, EndGameListener {

//this class is a collection of all the levels and allows for levels to 
//interact with each other
    private enum LevelNumber {
        LEVEL1(1), LEVEL2(2), LEVEL3(3), LEVEL4(4);

        private static final Map<Integer, LevelNumber> lookup
                = new HashMap<>();

        static {
            EnumSet.allOf(LevelNumber.class).forEach(ln -> {
                lookup.put(ln.getCode(), ln);
            });
        }

        private final int code;

        private LevelNumber(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static LevelNumber getLevelNumber(int code) {
            return lookup.get(code);
        }
    }

    private LevelNumber levelNumber;
    private World world;
    private Level level;
    private UserView view;
    private Player player;
    private KeyboardHandler kh;
    private final JLayeredPane layeredPane;
    private final int resolutionX, resolutionY;
    private MouseHandler mh;
    private Runnable r1, r2;
    private final Semester2Java game;

    public Levels(JLayeredPane layeredPane, int resolutionX, int resolutionY, Semester2Java game) {
        this.layeredPane = layeredPane;
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        view = new UserView(new World(), resolutionY, resolutionY);
        player = new Player(new World(), this);
        level = new Level();
        mh = new MouseHandler(view, level, player);        
        kh = new KeyboardHandler(level, player, layeredPane);
        this.game=game;
        levelNumber = LevelNumber.LEVEL1;
        changeLevel();
    }

    private void changeLevel() {
        Level tempLevel;
        switch (levelNumber) {
            case LEVEL1:
                tempLevel=new Level1();
                nextLevel(tempLevel);
                
                initializeView();
                level.start();
                break;

            case LEVEL2:
                tempLevel = new Level2();
                nextLevel(tempLevel);
                
                layeredPane.remove(view);
                initializeView();
                layeredPane.add(view,0);

                System.out.println("Im on a new level");
                break;

            case LEVEL3:
                level.stop();
                System.out.println("make level3!");
                break;
                
            case LEVEL4:
                level.stop();
                nextLevel(new Level());
                System.out.println("make level4!");
                break;

            default:
                System.out.println("handle this pls");
        }

    }

    private void nextLevel(Level level) {
        this.level.removeChangeLevelListener(this);
        this.level.removeEndGameListener(this);
        this.level.removeStepListener(this);
        this.level = level;
        this.level.addChangeLevelListener(this);
        this.level.addEndGameListener(this);
        this.level.addStepListener(this);
        System.out.println(this.level.isRunning());
        initializePlayer(this.level);    
//        game.getFrame().removeKeyListener(kh);
        kh.setWorld(player.getWorld());
        kh.setPlayer(player);
//        game.getFrame().addKeyListener(kh);
        level.start();

    }

    private void initializePlayer(Level level) {
        player.cleanup();
        player = new Player(level, this);
        player.putOn(level.getBody("start"));
        player.setAngle(0);
        player.getHealthPanel().setBounds(20, 5, resolutionX - 20, 50);
        player.getProjectilePanel().setBounds(20, player.getHealthPanel().getHeight() + 5, resolutionX - 20, 50);
        layeredPane.add(player.getHealthPanel(),0);
        layeredPane.add(player.getProjectilePanel(),0);
    }

    private void initializeView() {
        view.removeMouseListener(mh);
        view = new UserView(level, resolutionX, resolutionY);
        view.setBounds(0, 0, resolutionX, resolutionY);
        view.setOpaque(false);
        mh = new MouseHandler(view, level, player);
        view.addMouseListener(mh);
        kh.setPlayer(player);
    }

    public void incrementLevel() {
        //TODO add logic to make sure level is less than total levels in enum
        levelNumber = LevelNumber.getLevelNumber(levelNumber.getCode() + 1);
        changeLevel();
    }

    public Level getLevel() {
        return level;
    }

    public void setView(UserView view) {
        this.view = view;
    }

    public UserView getView() {
        return view;
    }

    public Player getPlayer() {
        return player;
    }
    
    public KeyboardHandler getKeyboardHandler(){
        return kh;
    }
    
    public JLayeredPane getLayeredPane(){
        return layeredPane;
    }
    

    //abstract event methods
    @Override
    public void changeLevel(EventObject e) {
        incrementLevel();
    }

    @Override
    public void preStep(StepEvent e) {
    }

    @Override
    public void postStep(StepEvent e) {
        //the xaxis of the view will always follow the player
        Vec2 newCentre = new Vec2(player.getPosition().x, view.getCentre().y);

//        double distance = Math.abs(view.getCentre().y - player.getPosition().y);
        if (player.canJump() && player.getLinearVelocity().y == 0) {
            newCentre.y = player.getPosition().y;
            player.setDefaultImage();
        }
        if (!player.canJump() && player.getLinearVelocity().y < -0.5f) {
            player.jumpDown();
        }

        if (player.getLinearVelocity().y > 10) {
            player.setLinearVelocity(new Vec2(player.getLinearVelocity().x, 9));
        }

        view.setCentre(newCentre);

        player.drawPlayerShots();
        player.drawPlayerHealth();
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    @Override
    public void endGame(EventObject e) {
        System.out.println("game over");
        world.stop();

    }

}