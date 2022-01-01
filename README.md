# trconv
Tractography file converter

Converts files between various tractography formats:

Supported files
  - TCK (MRtrix) files
  - TRK (TrackVis) files
  - VTK files (future?)
  - Please suggest others ...

Example usages

  TO-TRK
  
    java -jar trconv.jar --to-trk /home/fred/data/inputs/input.tck /data/outputs/output

      Do a conversion from a TCK file to a TRK file. Read file INPUT.TCK file from given path. Write
      OUTPUT.TRK in /data/outputs

    java -jar trconv.jar --to-trk /home/fred/data/inputs /data/outputs

      Do a bunch of conversions from tractography files to a TRK files. Read each file from /home/fred/data/inputs
      and convert the tractography files there to TRK files in the /data/outputs directory. Keep the base file
      name intact (e.g. GOTZ.tck -> GOTS.trk).
  
    java -jar trconv.jar --to-trk input.tck .
  
      Do a conversion from INPUT.TCK to INPUT.TRK where INPUT.TRK will be placed in the current directory.

    java -jar trconv.jar --to-trk input.tck output.trk 
  
      Do a conversion from INPUT.TCK to OUTPUT.TRK where OUTPUT.TRK will be placed in the current directory.


  TO-TCK
  
    java -jar trconv.jar --to-tck /home/fred/data/inputs/input.trk /data/outputs/output 

      Do a conversion from a TRK file to a TCK file. Read file INPUT.TRK file from given path. Write
      OUTPUT.TCK in /data/outputs

    java -jar trconv.jar --to-tck /home/fred/data/inputs /data/outputs

      Do a bunch of conversions from tractography files to a TCK files. Read each file from /home/fred/data/inputs
      and convert the tractography files there to TCK files in the /data/outputs directory. Keep the base file
      name intact (e.g. GOTZ.trk -> GOTS.tck).

    java -jar trconv.jar --to-tck input.trk . 
  
      Do a conversion from INPUT.TRK to INPUT.TCK where INPUT.TCK will be placed in the current directory.

    java -jar trconv.jar --to-tck input.trk output.tck 
  
      Do a conversion from INPUT.TRK to OUTPUT.TCK where OUTPUT.TCK will be placed in the current directory.
  
  
  
  