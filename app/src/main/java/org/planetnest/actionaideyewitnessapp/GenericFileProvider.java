package org.planetnest.actionaideyewitnessapp;

import android.support.v4.content.FileProvider;

/**
 * @author Banjo Mofesola Paul
 *         Chief Developer, Planet NEST
 *         mofesolapaul@planetnest.org
 *         28/11/2017 11:24
 *
 *         This class is useful for resolving the FileUriExposedException that arises
 *         when your try to open a file on higher android versions with the file:/// url
 *         style
 */

public class GenericFileProvider extends FileProvider {
}
