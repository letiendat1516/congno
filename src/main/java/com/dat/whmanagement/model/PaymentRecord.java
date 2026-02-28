package com.dat.whmanagement.model;

import java.time.LocalDate;

/** Một lần thanh toán công nợ */
public class PaymentRecord {

    private int       id;
    private LocalDate paymentDate;
    private String    targetType;   // CUSTOMER / SUPPLIER
    private int       targetId;
    private double    amount;
    private String    note;

    public PaymentRecord() {}

    public int       getId()          { return id;          }
    public void      setId(int id)    { this.id = id;       }

    public LocalDate getPaymentDate()              { return paymentDate;       }
    public void      setPaymentDate(LocalDate d)   { this.paymentDate = d;     }

    public String    getTargetType()               { return targetType;        }
    public void      setTargetType(String t)       { this.targetType = t;      }

    public int       getTargetId()                 { return targetId;          }
    public void      setTargetId(int id)           { this.targetId = id;       }

    public double    getAmount()                   { return amount;            }
    public void      setAmount(double a)           { this.amount = a;          }

    public String    getNote()                     { return note;              }
    public void      setNote(String n)             { this.note = n;            }
}

