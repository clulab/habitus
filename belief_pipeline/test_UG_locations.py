from tpi_location_stage import TpiLocationStage

if __name__ == "__main__":
    locations_file_name: str = "./belief_pipeline/UG.tsv"
    location_stage = TpiLocationStage(locations_file_name)
    latitudes = location_stage.names_to_latitudes
    longitudes = location_stage.names_to_longitudes
    print(latitudes)
    print(longitudes)
