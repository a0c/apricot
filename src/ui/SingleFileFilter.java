package ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Randy
 * Date: 21.07.2006
 * Time: 19:53:32
 * To change this template use File | Settings | File Templates.
 */
public class SingleFileFilter extends FileFilter {
    private String acceptedExtension;

    // Like "AGM", "TST" etc.
    public String getAcceptedExtension() {
        return acceptedExtension;
    }

    public SingleFileFilter(String acceptedExtension, String title) {
        this.acceptedExtension = acceptedExtension;
    }

    //Accept all directories and all agm files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = GetExtension(f);
        if (extension != null) {
            return extension.equals(getAcceptedExtension());
        }

        return false;
    }

    private String GetExtension(File f) {
        int lastIndex = f.getName().lastIndexOf(".");
        return f.getName().substring(lastIndex + 1);
    }

    //The description of this filter
    public String getDescription() {
        return "*." + getAcceptedExtension();
    }
}