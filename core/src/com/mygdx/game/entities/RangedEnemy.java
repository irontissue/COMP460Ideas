package com.mygdx.game.entities;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.entities.userdata.CharacterData;
import com.mygdx.game.equipment.Equipment;
import com.mygdx.game.equipment.ranged.BadGun;
import com.mygdx.game.states.PlayState;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.b2d.BodyBuilder;

public class RangedEnemy extends Schmuck {
    public static final int ENTITY_TYPE = Constants.EntityTypes.RANGED_ENEMY;
    public float maxSpeed = 4.0f;

    public float randSpeed;

    //enemy's speed randomly varies to avoid total stacking. Obv change this later when implementing actual ai.
    public float randSpeedCd = 3.0f;
    public float randSpeedCdCount = 0.0f;


    public Equipment weapon;

    public RangedEnemy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
                 float startX, float startY) {
        super(state, world, camera, rays, w, h, startX, startY);
        weapon = new BadGun(this);
        randSpeed = maxSpeed;
    }

    public RangedEnemy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, float w, float h,
                       float startX, float startY, String id) {
        super(state, world, camera, rays, w, h, startX, startY, id);
        weapon = new BadGun(this);
        randSpeed = maxSpeed;
    }

    /**
     * Create the enemy's body and initialize playerNumber's user data.
     */
    public void create() {
        this.bodyData = new CharacterData(world, this);
        this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, false, true, Constants.Filters.BIT_ENEMY,
                (short) (Constants.Filters.BIT_WALL | Constants.Filters.BIT_SENSOR | Constants.Filters.BIT_PROJECTILE | Constants.Filters.BIT_PLAYER | Constants.Filters.BIT_ENEMY),
                Constants.Filters.ENEMY_HITBOX, false, bodyData);
    }

    @Override
    public void controller(float delta) {
        Vector2 player = state.getPlayer().getBody().getPosition();

        int distFromPlayer = 8;
        if(body.getPosition().x < player.x - distFromPlayer) {
            desiredXVel = randSpeed;
        } else if (body.getPosition().x > player.x + distFromPlayer) {
            desiredXVel = -randSpeed;
        }

        if(body.getPosition().y < player.y - distFromPlayer) {
            desiredYVel = randSpeed;
        } else if (body.getPosition().y > player.y + distFromPlayer) {
            desiredYVel = -randSpeed;
        }

        Vector3 target = new Vector3(state.getPlayer().getBody().getPosition().x, state.getPlayer().getBody().getPosition().y, 0);
        camera.project(target);
        useToolStart(delta, weapon, Constants.Filters.ENEMY_HITBOX, (int)target.x, (int)target.y, true);


        if (randSpeedCdCount > randSpeedCd) {
            randSpeedCdCount -= randSpeedCd;
            randSpeed = (float) (maxSpeed * (Math.random())) * 2;
        }

        randSpeedCdCount += delta;

        super.controller(delta);
    }

    /**
     * Deletes enemy. Currently also increments game score.
     */
    public void dispose() {
        state.incrementScore(1);
        super.dispose();
    }
}
