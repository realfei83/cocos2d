package org.cocos2d.transitions;

import org.cocos2d.nodes.CCDirector;
import org.cocos2d.layers.CCScene;
import org.cocos2d.types.CGPoint;
import org.cocos2d.events.CCTouchDispatcher;

import javax.microedition.khronos.opengles.GL10;

public class TransitionScene extends CCScene {

    protected static final int kSceneFade = 0xFADEFADE;


    /**
     * Orientation Type used by some transitions
     */
    public interface Orientation {
        /// An horizontal orientation where the Left is nearer
        public static int kOrientationLeftOver = 0;
        /// An horizontal orientation where the Right is nearer
        public static int kOrientationRightOver = 1;
        /// A vertical orientation where the Up is nearer
        public static int kOrientationUpOver = 0;
        /// A vertical orientation where the Bottom is nearer
        public static int kOrientationDownOver = 1;
    }

    /**
     * Base class for Transition scenes
     */
    protected CCScene inScene;
    protected CCScene outScene;
    protected float duration;
    protected boolean inSceneOnTop;

    /**
     * creates a base transition with duration and incoming scene
     */
    public static TransitionScene transition(float t, CCScene s) {
        return new TransitionScene(t, s);
    }

    /**
     * initializes a transition with duration and incoming scene
     */
    protected TransitionScene(float t, CCScene s) {
        assert s != null : "Argument scene must be non-null";

        duration = t;

        // Don't retain them, it will be reatined when added
        inScene = s;
        outScene = CCDirector.sharedDirector().getRunningScene();

        if (inScene == outScene) {
            throw new TransitionWithInvalidSceneException("Incoming scene must be different from the outgoing scene");
        }

        // disable events while transition
        CCTouchDispatcher.sharedDispatcher().setDispatchEvents(false);
        sceneOrder();

    }

    protected void sceneOrder() {
        // add both scenes
        inSceneOnTop = true;
    }

    public void draw(GL10 gl)
    {
        if( inSceneOnTop) {
            outScene.visit(gl);
            inScene.visit(gl);
        } else {
            inScene.visit(gl);
            outScene.visit(gl);
        }
    }

    public void finish() {
        /* clean up */
        inScene.setVisible(true);
        inScene.setPosition(CGPoint.zero());
        inScene.setScale(1.0f);
        inScene.setRotation(0.0f);
        inScene.getCamera().restore();

        outScene.setVisible(false);
        outScene.setPosition(CGPoint.zero());
        outScene.setScale(1.0f);
        outScene.setRotation(0.0f);
        outScene.getCamera().restore();

        schedule("setNewScene", 0);
    }

    public void setNewScene(float dt) {

        unschedule("setNewScene");

        CCDirector.sharedDirector().replaceScene(inScene);

        // enable events after transition
        CCTouchDispatcher.sharedDispatcher().setDispatchEvents(true);

        // issue #267
        outScene.setVisible(true);

    }

    /**
     * used by some transitions to hide the outter scene
     */
    public void hideOutShowIn() {
        inScene.setVisible(true);
        outScene.setVisible(false);
    }


    // custom onEnter

    @Override
    public void onEnter() {
        super.onEnter();
        inScene.onEnter();
        // outScene should not receive the onEnter callback
    }

    // custom onExit

    @Override
    public void onExit() {
        super.onExit();
        outScene.onExit();

        // inScene should not receive the onExit callback
        // only the onEnterTransitionDidFinish
        inScene.onEnterTransitionDidFinish();
    }

    @Override
    public void onEnterTransitionDidFinish() {
        super.onEnterTransitionDidFinish();
    }

    
    static class TransitionWithInvalidSceneException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = -1743490059348093939L;

		public TransitionWithInvalidSceneException(String reason) {
            super(reason);
        }
    }
}

