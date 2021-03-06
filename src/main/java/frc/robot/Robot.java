package frc.robot;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ctre.phoenix.motorcontrol.can.SlotConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.subsystems.ClimbSystem;
import frc.robot.subsystems.DriveSystem;
import frc.robot.subsystems.IntakeSystem;
import frc.robot.subsystems.ShooterSystem;

public class Robot extends TimedRobot {
	private Logger stateLogger = LogManager.getLogger("robot_state");
	private Logger robotLogger = LogManager.getLogger("robot");

	// TODO: this seems like it would be better encapsulated by the robot state
	// updater.
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * The robot's configured autonomous command.
	 */
	private Command autoCommand = null;

	// TODO: While it's purpose and intention is understood, the 'container'
	// concept still doesn't sit well. It would be ideal if we could determine a
	// better approach and take that instead. This is a just a note to remind us
	// to consider it as we move forward.
	private RobotContainer container = null;

	/**
	 * Tracks the current state of the robot.
	 */
	private RobotState robotState = null;

	/**
	 * Thread pool for handling interval based tasks that are outside of the
	 * typical robot lifecyle. In other words, things that should not be on the
	 * robot's main loop/thread.
	 */
	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

	/**
	 * The robot's drive train subsystem.
	 */
	public final static DriveSystem drive = new DriveSystem();

	/**
	 * The robot's intake subsystem.
	 */
	public final static IntakeSystem intake = new IntakeSystem();

	/**
	 * The robot's climber subsystem.
	 */
	public final static ClimbSystem climber = new ClimbSystem();

	/**
	 * The robot's shooter subsystem.
	 */
	public final static ShooterSystem shooter = new ShooterSystem();

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		// Instantiate our RobotContainer.  This will perform all our button
		// bindings, and put our autonomous chooser on the dashboard.
		this.container = new RobotContainer();

		// TODO: since subsystems are now static properties of the robot, do we
		// really need to construct this object in this manner?
		this.robotState = new RobotState()
			// .withPDP(new PowerDistributionPanel())
			.withDriveSystem(Robot.drive)
			.withIntakeSystem(Robot.intake);

		// TODO: should this be refactored such that the runnable is defined
		// elsewhere?
		// this.executor.scheduleAtFixedRate(
		// 	() -> {
		// 		this.robotState.update();
		// 		try {
		// 			String state = this.mapper.writeValueAsString(this.robotState);
		// 			this.stateLogger.info(state);
		// 		} catch (JsonProcessingException e) {
		// 			this.stateLogger.error(e.toString());
		// 		}
		// 	},
		// 	0,   // initial delay
		// 	100, // delay
		// 	TimeUnit.MILLISECONDS
		// );

		SlotConfiguration[] slots = Robot.drive.getPID();

		for (int i = 0; i < slots.length; i++) {
			this.robotLogger.info("Slot: {} - P: {} I: {} D: {} F: {}",
				i, slots[i].kP, slots[i].kI, slots[i].kD, slots[i].kF
			);
		}
	}

	@Override
	public void robotPeriodic() {
		CommandScheduler.getInstance().run();
	}

	/**
	 * This function is called once each time the robot enters Disabled mode.
	 */
	@Override
	public void disabledInit() {
	}

	@Override
	public void disabledPeriodic() {
	}

	/**
	 * This autonomous runs the autonomous command selected by your
	 * {@link RobotContainer} class.
	 */
	@Override
	public void autonomousInit() {
		Robot.drive.setPIDF(
			DriveSystem.POSITION_P,
			DriveSystem.POSITION_I,
			DriveSystem.POSITION_D,
			DriveSystem.POSITION_FEED_FORWARD
		);
		// Robot.drive.setPIDF(
		// 	DriveSystem.VELOCITY_P,
		// 	DriveSystem.VELOCITY_I,
		// 	DriveSystem.VELOCITY_D,
		// 	DriveSystem.VELOCITY_FEED_FORWARD
		// );

		Robot.drive.resetAngle();
		this.robotLogger.info("reset drive system angle: {}", Robot.drive.getAngle());

		Robot.drive.resetPosition();

		this.autoCommand = this.container.getAutonomousCommand("slalom");

		// schedule the autonomous command (example)
		if (this.autoCommand != null) {
			this.autoCommand.schedule();
		}
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {

	}

	@Override
	public void teleopInit() {
		Robot.drive.setPIDF(
			DriveSystem.VELOCITY_P,
			DriveSystem.VELOCITY_I,
			DriveSystem.VELOCITY_D,
			DriveSystem.VELOCITY_FEED_FORWARD
		);

		Robot.drive.resetAngle();
		Robot.drive.resetPosition();

		// This makes sure that the autonomous stops running when
		// teleop starts running. If you want the autonomous to
		// continue until interrupted by another command, remove
		// this line or comment it out.
		if (this.autoCommand != null) {
			this.autoCommand.cancel();
		}
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
	}

	@Override
	public void testInit() {
		// Cancels all running commands at the start of test mode.
		CommandScheduler.getInstance().cancelAll();
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}

	@Override
	public void startCompetition() {
		super.startCompetition();
	}
}
