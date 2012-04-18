package net.shrine.adapter.dao;

/**
 * @author Bill Simons
 * @date Nov 30, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class UserThreshold
{
    private String username;
    private Integer threshold;

    public UserThreshold(String username, Integer threshold)
    {
        this.username = username;
        this.threshold = threshold;
    }

    public UserThreshold()
    {
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public Integer getThreshold()
    {
        return threshold;
    }

    public void setThreshold(Integer threshold)
    {
        this.threshold = threshold;
    }
}
