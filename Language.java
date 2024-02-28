
import java.lang.reflect.Field;

import java.util.Vector;

import bookcase.TextFile;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * This class is used to store all language strings and read translations
 * from configuration files.
 */

public class Language extends Entity
{
	private final static TextFile file = new TextFile();
	private final static Vector<String> fieldNames = new Vector<String>();
	static
	{
		Field[] fields = Language.class.getDeclaredFields();
		for (Field field : fields)
		{
			if (field.getName().endsWith("Default"))
			{
				fieldNames.add(field.getName().substring(0,
					field.getName().length() - 7));
			}
		}
	}

	private final static StringBuffer
		buildMessageBuffer = new StringBuffer();

	public static String buildMessage(String template, Object... arguments)
	{
		buildMessageBuffer.setLength(0);
		buildMessageBuffer.append(template);
		boolean replaced = true;

		// Max. level of replacement recursion is 16
		for (int i = 0; i < 16 && replaced; ++i)
		{
			int number = 1; replaced = false;
			for (Object argument : arguments)
			{
				int indexOf = buildMessageBuffer.indexOf("%" + number);
				if (-1 != indexOf)
				{
					if (null == argument)
						buildMessageBuffer.replace(indexOf, indexOf + 2, "");
					else
						buildMessageBuffer.replace(indexOf, indexOf + 2,
							argument.toString());
					replaced = true;
				}
				++number;
			}
		}

		return buildMessageBuffer.toString();
	}


	/*
	public String xxx;
	public final static String xxxDefault =
		"xxx";
	*/

	public String menu;
	public final static String menuDefault = "Menu";

	public String open;
	public final static String openDefault = "Open";

	public String save;
	public final static String saveDefault = "Save";

	public String exit;
	public final static String exitDefault = "Exit";

	public String about;
	public final static String aboutDefault = "About";

	public String menuMnemonic;
	public final static String menuMnemonicDefault = null;

	public String openMnemonic;
	public final static String openMnemonicDefault = null;

	public String saveMnemonic;
	public final static String saveMnemonicDefault = null;

	public String exitMnemonic;
	public final static String exitMnemonicDefault = null;

	public String aboutMnemonic;
	public final static String aboutMnemonicDefault = null;

	public String openDialogTitle;
	public final static String openDialogTitleDefault = "Open…";

	public String saveDialogTitle;
	public final static String saveDialogTitleDefault = "Save…";

	public String defaultSuccess;
	public final static String defaultSuccessDefault = "OK";

	public String defaultFailure;
	public final static String defaultFailureDefault = "Error";

	public String readSuccess;
	public final static String readSuccessDefault = "Reading was successful.";

	public String writeSuccess;
	public final static String writeSuccessDefault = "Writing was successful.";


	public String alertOperationFailed;
	public final static String alertOperationFailedDefault =
		"Operation failed.";

	public String alertOperationDenied;
	public final static String alertOperationDeniedDefault =
		"This operation has been denied.";

	public String alertEntryAlreadyPlaced;
	public final static String alertEntryAlreadyPlacedDefault =
		"Entry is already placed somewhere.";

	public String alertEntryTooHeavy;
	public final static String alertEntryTooHeavyDefault =
		"Entry is too heavy.";

	public String alertEntryTooLarge;
	public final static String alertEntryTooLargeDefault =
		"Entry is too large.";

	public String alertEntryNotInPlace;
	public final static String alertEntryNotInPlaceDefault =
		"Entry is not there.";

	public String alertEntryRecursivePlacement;
	public final static String alertEntryRecursivePlacementDefault =
		"Entry cannot be put inside itself.";


	public String failureTokenNotFound;
	public final static String failureTokenNotFoundDefault =
		"Unknown word: %1";

	public String failureInvalidOperator;
	public final static String failureInvalidOperatorDefault =
		"Invalid link word: %1";

	public String failureUnknownCommand;
	public final static String failureUnknownCommandDefault =
		"Unknown command: %1";

	public String failureInvalidNumberOfTokens;
	public final static String failureInvalidNumberOfTokensDefault =
		"Invalid number of parts of the command: %1";

	public String failureCommandLineTooLong;
	public final static String failureCommandLineTooLongDefault =
		"Command line is too long.";

	public String failureUnknownLocation;
	public final static String failureUnknownLocationDefault =
		"Unknow location: %1";

	public String failureUnknownMean;
	public final static String failureUnknownMeanDefault =
		"Unknow mean: %1";

	public String failureUnknownItem;
	public final static String failureUnknownItemDefault =
		"Unknow item: %1";

	public String failureMissingLocationForCommand;
	public final static String failureMissingLocationForCommandDefault =
		"No location has been specified for command: %1";

	public String failureMissingItemForCommand;
	public final static String failureMissingItemForCommandDefault =
		"No item has been specified for command: %1";

	public String failureMissingContainerForCommand;
	public final static String failureMissingContainerForCommandDefault =
		"No container has been specified for command: %1";

	public String failureItemNotHere;
	public final static String failureItemNotHereDefault =
		"Item is not here: %1";

	public String failureItemNotInside;
	public final static String failureItemNotInsideDefault =
		"Item %1 is not inside item %2.";

	public String failureAvatarDoesNotHave;
	public final static String failureAvatarDoesNotHaveDefault =
		"Avatar does not have: %1";

	public String failureAvatarHaveAlready;
	public final static String failureAvatarHaveAlreadyDefault =
		"Avatar have already: %1";

	public String failureAvatarIsInNowhere;
	public final static String failureAvatarIsInNowhereDefault =
		"Avatar is in nowhere.";

	public String failureAvatarIsInside;
	public final static String failureAvatarIsInsideDefault =
		"Avatar is in item: %1";

	public String failureAvatarTooHeavy;
	public final static String failureAvatarTooHeavyDefault =
		"Avatar is too heavy.";

	public String failureAvatarTooLarge;
	public final static String failureAvatarTooLargeDefault =
		"Avatar is too large.";

	public String failureTransitionNotFound;
	public final static String failureTransitionNotFoundDefault =
		"Transition to %1 has not been foud.";

	public String failureMissingTarget;
	public final static String failureMissingTargetDefault =
		"A second entry in the meaning of target is required for this action!";

	public String failureMissingSource;
	public final static String failureMissingSourceDefault =
		"A second entry in the meaning of source is required for this action!";

	public String failureMissingTool;
	public final static String failureMissingToolDefault =
		"A second entry in the meaning of tool is required for this action!";

	public String failureItemNotVehicle;
	public final static String failureItemNotVehicleDefault =
		"This item is not a vehicle: %1";

	public String failureItemUnusable;
	public final static String failureItemUnusableDefault =
		"This item is not usable: %1";

	public String failureItemNoAction;
	public final static String failureItemNoActionDefault =
		"This action cannot be performed for item: %1";

	public String failureItemUnableExamine;
	public final static String failureItemUnableExamineDefault =
		"This item cannot be examined: %1";


	public String warningNullEntity;
	public final static String warningNullEntityDefault =
		"There is no active entity at line %2 in %1 for command: %3";

	public String warningNullCommand;
	public final static String warningNullCommandDefault =
		"Specified command (%3 – at line %2 in %1) is not definded at this time.";

	public String warningNullOperator;
	public final static String warningNullOperatorDefault =
		"Operator for specified command (%3 – at line %2 in %1) is not definded at this time.";

	public String warningOverridingAlias;
	public final static String warningOverridingAliasDefault =
		"Overriding alias at line %2 in %1: %3";

	public String warningAmbiguousAlias;
	public final static String warningAmbiguousAliasDefault =
		"Ambiguous alias at line %2 in %1: %3";

	public String warningDuplicateAlias;
	public final static String warningDuplicateAliasDefault =
		"Duplicate alias at line %2 in %1: %3";

	public String warningAliasTokenConflict;
	public final static String warningAliasTokenConflictDefault =
		"Following token will supress existing alias, because it is in conflict with it: %1";


	public String syntaxError;
	public final static String syntaxErrorDefault =
		"Syntax error in %1 at line %2: %3";


	public String errorUnknownCommand;
	public final static String errorUnknownCommandDefault =
		"Unknown command at line %2 in %1: %3";

	public String errorNoSelection;
	public final static String errorNoSelectionDefault =
		"There is no selection!";

	public String errorImageNotFound;
	public final static String errorImageNotFoundDefault =
		"Image “%1” was not found!";

	public String errorAmbiguousTokens;
	public final static String errorAmbiguousTokensDefault =
		"There are ambiguous token at line %2 in the file %1: %3 (compare to %4)";

	public String errorDuplicateToken;
	public final static String errorDuplicateTokenDefault =
		"The token at line %2 in the file %1 is against the rules not unique: %3";

	public String errorUnexpectedCommand;
	public final static String errorUnexpectedCommandDefault =
		"Unexpected command found at line %2 in the file %1: %3";

	public String errorUnsupportedProperty;
	public final static String errorUnsupportedPropertyDefault =
		"Unsupported property found at line %2 in the file %1: %3";

	public String errorUnknownProperty;
	public final static String errorUnknownPropertyDefault =
		"Unknown property at line %2 in %1: %3";

	public String errorUnknownOwnerOfProperty;
	public final static String errorUnknownOwnerOfPropertyDefault =
		"The owner of property has not been found at line %2 in %1: %3";

	public String errorUnsupportedOperation;
	public final static String errorUnsupportedOperationDefault =
		"Operation requested at line %2 in the file %1 is not supported: %3";

	public String errorUnexpectedTransition;
	public final static String errorUnexpectedTransitionDefault =
		"The transition at line %2 in the file %1 cannot be defined nor modified, because there is no active definition of location.";

	public String errorUnknownHandler;
	public final static String errorUnknownHandlerDefault =
		"Unknown handler found at line %2 in the file %1: %3";

	public String errorUnexpectedEndOfFile;
	public final static String errorUnexpectedEndOfFileDefault =
		"Definition file %1 ended unexpectly at line %2.";

	public String errorLocationNotFound;
	public final static String errorLocationNotFoundDefault =
		"Location %3 not found in %1 at line %2.";

	public String errorItemNotFound;
	public final static String errorItemNotFoundDefault =
		"Item %3 not found in %1 at line %2.";

	public String errorTransitionSourceMissing;
	public final static String errorTransitionSourceMissingDefault =
		"The source location is missing for a transition in %1 at line %2.";

	public String errorTransitionTargetMissing;
	public final static String errorTransitionTargetMissingDefault =
		"The target location is missing for a transition in %1 at line %2.";

	public String errorTransitionNotFound;
	public final static String errorTransitionNotFoundDefault =
		"Transition between locations %3 and %4 has not been found in %1 at line %2.";

	public String errorUnknownEntity;
	public final static String errorUnknownEntityDefault =
		"Unknown entity in %1 at line %2: %3";

	public String errorNullPropertyHolder;
	public final static String errorNullPropertyHolderDefault =
		"There is no active entity in %1 at line %2 for specified property: %3";

	public String errorTriggerNotFound;
	public final static String errorTriggerNotFoundDefault =
		"Trigger %3 not found in %1 at line %2.";

	public String errorUnknownComponent;
	public final static String errorUnknownComponentDefault =
		"Unknown component in %1 at line %2: %3";

	public String errorInvalidRedefinition;
	public final static String errorInvalidRedefinitionDefault =
		"The command or operator in %1 at line %2 is already defined and cannot be redefined: %3";

	public String errorInvalidNumberOfTokens;
	public final static String errorInvalidNumberOfTokensDefault =
		"Invalid number of tokens in %1 at line %2.";

	public String errorUnknownDefinition;
	public final static String errorUnknownDefinitionDefault =
		"Unknown definition in %1 at line %2: %3";

	public String errorExpressionSyntaxError;
	public final static String errorExpressionSyntaxErrorDefault =
		"Syntax error in expression in %1 at line %2: %3";

	public String errorRedefinitionDenied;
	public final static String errorRedefinitionDeniedDefault =
		"Redefinition at line %2 in script %1 is not allowed. Redefinitions must be on the very first lines of the script.";

	public String errorUnknownRedefinition;
	public final static String errorUnknownRedefinitionDefault =
		"Unknown redefinition at line %2 in %1: %3";

	public String errorUnknownUpdateTarget;
	public final static String errorUnknownUpdateTargetDefault =
		"Unknown update target at line %2 in %1: %3";

	public String errorInvalidPlacement;
	public final static String errorInvalidPlacementDefault =
		"Invalid placement of “%3” at line %2 in %1.";

	public String errorPlacementError;
	public final static String errorPlacementErrorDefault =
		"Placement error “%3” for %4 at line %2 in %1.";

	public String errorUnknownForVariant;
	public final static String errorUnknownForVariantDefault =
		"Unknown variant of “for” at line %2 in %1: %3";


	public Language(String name)
	{
		super(name.trim());

		try
		{
			file.openForReading(this.name + ".lng");

			for (String fieldName : fieldNames)
			{
				String defaultValue = null;
				try
				{
					defaultValue = Language.class.getDeclaredField(
						fieldName + "Default").get(null).toString();
				}
				catch (Exception e) { }

				try
				{
					Field field = Language.class.getDeclaredField(fieldName);
					field.set(this, file.readProperty(
						fieldName, defaultValue));
				}
				catch (Exception e)
				{
					System.out.println("  Language field: " + fieldName);
					DriverOutput.printError(e);
				}
			}

			file.close();
			System.out.println("Language read: " + this.name);
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
		}
	}


	public String parseMessage(String templateName, Object... arguments)
	{
		try
		{
			engine.reasonType = templateName;

			return buildMessage(
				Language.class.getDeclaredField(templateName).
					get(this).toString(), arguments);
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
		}

		return null;
	}
}
