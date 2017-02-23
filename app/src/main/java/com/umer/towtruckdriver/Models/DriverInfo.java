package com.umer.towtruckdriver.Models;

/**
 * Created by umer on 12/27/16.
 */

public class DriverInfo {

    private String Phone;

    private String liscenseno;

    private String status;

    private String CURP;

    private String RFC;

    private String Name;

    private String updated_at;

    private String Email;

    private String DisplayPicture;

    private String Address;

    private String Gender;

    private String dob;

    private String created_at;

    private String DriverId;

    private String rating;

    public String getPhone ()
    {
        return Phone;
    }

    public void setPhone (String Phone)
    {
        this.Phone = Phone;
    }

    public String getLiscenseno ()
    {
        return liscenseno;
    }

    public void setLiscenseno (String liscenseno)
    {
        this.liscenseno = liscenseno;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    public String getCURP ()
    {
        return CURP;
    }

    public void setCURP (String CURP)
    {
        this.CURP = CURP;
    }

    public String getRFC ()
    {
        return RFC;
    }

    public void setRFC (String RFC)
    {
        this.RFC = RFC;
    }

    public String getName ()
    {
        return Name;
    }

    public void setName (String Name)
    {
        this.Name = Name;
    }

    public String getUpdated_at ()
    {
        return updated_at;
    }

    public void setUpdated_at (String updated_at)
    {
        this.updated_at = updated_at;
    }

    public String getEmail ()
    {
        return Email;
    }

    public void setEmail (String Email)
    {
        this.Email = Email;
    }

    public String getDisplayPicture ()
    {
        return DisplayPicture;
    }

    public void setDisplayPicture (String DisplayPicture)
    {
        this.DisplayPicture = DisplayPicture;
    }

    public void setRating(String Rating){this.rating = rating;}

    public String getRating(){return rating;}

    public String getAddress ()
    {
        return Address;
    }

    public void setAddress (String Address)
    {
        this.Address = Address;
    }

    public String getGender ()
    {
        return Gender;
    }

    public void setGender (String Gender)
    {
        this.Gender = Gender;
    }

    public String getDob ()
    {
        return dob;
    }

    public void setDob (String dob)
    {
        this.dob = dob;
    }

    public String getCreated_at ()
    {
        return created_at;
    }

    public void setCreated_at (String created_at)
    {
        this.created_at = created_at;
    }

    public String getDriverId ()
    {
        return DriverId;
    }

    public void setDriverId (String DriverId)
    {
        this.DriverId = DriverId;
    }
}
